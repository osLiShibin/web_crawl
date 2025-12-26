package me.lbb.crawl.support

import me.lbb.crawl.entity.LinkCheckResult
import org.dromara.hutool.core.text.StrUtil
import org.dromara.hutool.core.util.RandomUtil
import org.slf4j.LoggerFactory
import org.springframework.util.AntPathMatcher
import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Request
import us.codecraft.webmagic.Site
import us.codecraft.webmagic.processor.PageProcessor
import java.time.LocalDateTime


/**
 * Description: 处理器
 *
 * @author csy
 * Created on 2025/12/5 17:35.
 */
class LinkCheckPageProcessor(
    private val excludePatterns: List<String>,
    private val baseUrl: String,
    private val taskId: Long,
    private val xpath: String,
) : PageProcessor {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val pathMatcher = AntPathMatcher()

    override fun getSite(): Site? {
        return Site.me()
            .setRetryTimes(1)
            .setSleepTime(500 + RandomUtil.randomInt(500))
            .setTimeOut(10000)
            .setDisableCookieManagement(false)
            .setCycleRetryTimes(1)
            .setUserAgent(RequestRateLimiter.getRandomUserAgent())
            .setAcceptStatCode(setOf(200, 204, 301, 302, 400, 401, 403, 404, 406, 412, 500, 502, 503, 504))
    }

    override fun process(page: Page) {
        val currentUrl = page.url.get()
        val currentDepth = getDepth(page)
        val parentUrl = getParent(page)
        val isHtml = isHtml(page)
        val startTime = getStartTime(page)
        val endTime = getEndTime(page)

        // 1. check if we should process this page (exclusion)
        if (shouldExclude(currentUrl)) {
            page.setSkip(true)
            return
        }

        val result = LinkCheckResult()
        result.taskId = taskId
        result.url = currentUrl
        result.parentUrl = parentUrl
        result.checkTime = LocalDateTime.now()
        result.responseStatus = page.statusCode
        result.checkDurationMs = if (endTime != -1L && startTime != -1L) endTime - startTime else -1L
        // Determine type and external status
        result.isExternal = UrlHelper.isExternalLink(baseUrl, currentUrl)

        result.type = if (isHtml) "LINK" else "SOURCE"

        val canExtractSubLinks = page.statusCode >= 200 && page.statusCode < 400
        result.isValid = canExtractSubLinks

        // Extract content if XPath is provided and it is an HTML page
        if (isHtml && canExtractSubLinks && StrUtil.isNotBlank(xpath)) {
            try {
                val extracted: String? = page.getHtml().xpath(xpath).toString()
                result.extractedData = extracted
            } catch (e: Exception) {
                logger.error("Error extracting XPath data", e)
            }
        }
        page.putField("result", result)
        CacheHelper.put(currentUrl, result)
        if (isHtml && canExtractSubLinks && currentDepth < 2) {
            // 获取所有连接
            val linksToVisit = HashSet<String>()
            val resourcesToCheck = HashSet<String>()
            linksToVisit.addAll(page.html.links().all().filter { UrlHelper.isUrlValid(it) }
                .filter {
                    if (UrlHelper.isPageLink(it)) {
                        return@filter true
                    } else {
                        resourcesToCheck.add(it)
                        return@filter false
                    }
                }
                .map { UrlHelper.toAbsoluteUrl(baseUrl, it) }
            )

            val html = page.html
            resourcesToCheck.addAll(html.css("link", "href").all().map { UrlHelper.toAbsoluteUrl(baseUrl, it) }) // CSS
            resourcesToCheck.addAll(html.css("script", "src").all().map { UrlHelper.toAbsoluteUrl(baseUrl, it) }) // JS
            resourcesToCheck.addAll(html.css("img", "src").all().filter { UrlHelper.isUrlValid(it) }
                .map { UrlHelper.toAbsoluteUrl(baseUrl, it) }) // Images
            resourcesToCheck.addAll(
                html.css("video", "src").all().map { UrlHelper.toAbsoluteUrl(baseUrl, it) }) // Video
            resourcesToCheck.addAll(
                html.css("source", "src").all().map { UrlHelper.toAbsoluteUrl(baseUrl, it) }) // Video sources
            resourcesToCheck.addAll(
                UrlHelper.extractStyleImageUrls(html.document.select("style"))
                    .map { UrlHelper.toAbsoluteUrl(baseUrl, it) })
            resourcesToCheck.addAll(
                UrlHelper.extractScriptUrls(html.document.select("script"))
                    .map { UrlHelper.toAbsoluteUrl(baseUrl, it) })
            val checked = ArrayList<LinkCheckResult>()

            checked.addAll(linksToVisit.mapNotNull { CacheHelper.get(it) }.map {
                it.parentUrl = currentUrl
                it
            })
            checked.addAll(resourcesToCheck.mapNotNull { CacheHelper.get(it) }.map {
                it.parentUrl = currentUrl
                it
            })
            page.putField("checkedList", checked)

            linksToVisit
                .filter { !shouldExclude(it) }
                .forEach {
                    page.addTargetRequest(
                        Request(it)
                            .putExtra("parentUrl", currentUrl)
                            .putExtra("depth", currentDepth + 1)
                            .putExtra("isHtml", true)
                    )
                }

            resourcesToCheck
                .filter { it ->
                    // 如果缓存中存在
                    val checkResult = CacheHelper.get(it)
                    if (checkResult != null) {
                        // 直接保存
                        false
                    }
                    true
                }
                .filter { !shouldExclude(it) }
                .forEach {
                    page.addTargetRequest(
                        Request(it)
                            .putExtra("parentUrl", currentUrl)
                            .putExtra("depth", currentDepth + 1)
                            .putExtra("isHtml", false)
                    )
                }
        }
    }

    private fun isHtml(page: Page): Boolean {
        return page.request.getExtra<Boolean>("isHtml") ?: false
    }

    private fun getDepth(page: Page): Int {
        val depth = page.request.getExtra<Int>("depth")
        return depth ?: 1
    }

    private fun getStartTime(page: Page): Long {
        return page.request.getExtra<Long>("startTime") ?: -1
    }

    private fun getEndTime(page: Page): Long {
        return page.request.getExtra<Long>("endTime") ?: -1
    }

    private fun getParent(page: Page): String? {
        return page.request.getExtra<String>("parentUrl")
    }

    private fun shouldExclude(url: String): Boolean {
        for (pattern in excludePatterns) {
            if (pathMatcher.match(pattern, url)) {
                return true
            }
        }
        return false
    }

}