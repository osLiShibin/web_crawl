package me.lbb.crawl.support

import org.dromara.hutool.core.lang.Validator
import org.dromara.hutool.core.net.url.UrlUtil
import org.dromara.hutool.core.regex.PatternPool
import org.dromara.hutool.core.text.StrUtil
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import us.codecraft.webmagic.utils.UrlUtils
import java.util.regex.Pattern

/**
 * Description: 辅助类
 *
 * @author csy
 * Created on 2025/12/6 8:58.
 */
object UrlHelper {

    //提取style内图片链接（background-image: url(xxx)）
    val STYLE_IMAGE_PATTERN = Pattern.compile("url\\(['\"]?(.*?)['\"]?\\)", Pattern.CASE_INSENSITIVE)

    // 提取script内AJAX地址（url: 'xxx' 或 url="xxx"）
    val SCRIPT_URL_PATTERN = Pattern.compile("url\\s*[:=]\\s*['\"](.*?)['\"]", Pattern.CASE_INSENSITIVE)

    // ========== 核心：页面链接后缀（可根据业务扩展） ==========
    private val PAGE_LINK_PATTERN: Pattern = Pattern.compile(
        "^.*\\.(html|htm|php|jsp|asp|aspx|do|action|shtml|jspx|ftl)$",
        Pattern.CASE_INSENSITIVE
    )
    // 资源链接后缀（用于反向排除）
    private val RESOURCE_LINK_PATTERN: Pattern = Pattern.compile(
        "^.*\\.(css|js|png|jpg|jpeg|gif|bmp|svg|ico|mp4|avi|mp3|woff|woff2|ttf|eot|otf|zip|rar|pdf|doc(x)?|xls(x)?)$",
        Pattern.CASE_INSENSITIVE
    )

    fun isAbsoluteUrl(url: String?): Boolean {
        if (StrUtil.isEmpty(url)) {
            return false
        }
        return Validator.isMatchRegex(PatternPool.URL_HTTP, url)
    }

    fun toAbsoluteUrl(baseUrl: String, relativeUrl: String): String {
        if (isAbsoluteUrl(relativeUrl)) {
            return relativeUrl.trim()
        }
        // 没有协议，相同域名，则补充协议
        if (StrUtil.startWithAnyIgnoreCase(relativeUrl, getDomain(baseUrl))) {
            return UrlUtil.url(baseUrl).protocol + "://" + relativeUrl
        }
        val newUrl = StrUtil.replace(relativeUrl, "..", "")
        return UrlUtil.getURL(UrlUtil.url(baseUrl), newUrl).toString()
    }

    /**
     * 获取域名小写
     */
    fun getDomain(url: String): String {
        try {
            val u = UrlUtil.url(UrlUtils.fixIllegalCharacterInUrl(url))
            return u.host.lowercase()
        } catch (e: Exception) {
            return ""
        }
    }

    /**
     * 检查url是否有效
     */
    fun isUrlValid(url: String?): Boolean {
        if (url == null || url.trim().isEmpty()) return false
        return StrUtil.isNotBlank(url)
                && !url.startsWith("javascript:")
                && !url.startsWith("data:image")
                && !url.startsWith("mailto:")
                && !url.startsWith("#");
    }

    /**
     * 判断是否是外部链接
     */
    fun isExternalLink(baseUrl: String?, targetUrl: String?): Boolean {
        if (baseUrl == null || targetUrl == null) return false
        return getDomain(baseUrl) != getDomain(targetUrl)
    }

    /**
     * 提取style标签内的图片链接
     */
    fun extractStyleImageUrls(styleElements: Elements): List<String> {
        val urls = ArrayList<String>()
        if (styleElements.isEmpty()) return urls
        for (styleElement in styleElements) {
            for (node in styleElement.childNodes()) {
                if (node is TextNode) {
                    val matcher = STYLE_IMAGE_PATTERN.matcher(node.text())
                    while (matcher.find()) {
                        val url = matcher.group(1).trim()
                        if (isUrlValid(url)) {
                            urls.add(url)
                        }
                    }
                }
            }
        }
        return urls
    }

    /**
     * 提取script标签内的AJAX链接
     */
    fun extractScriptUrls(scriptElements: Elements): List<String> {
        val urls = ArrayList<String>()
        if (scriptElements.isEmpty()) return urls
        for (scriptElement in scriptElements) {
            for (node in scriptElement.childNodes()) {
                if (node is TextNode) {
                    val matcher = SCRIPT_URL_PATTERN.matcher(node.text())
                    while (matcher.find()) {
                        val url = matcher.group(1).trim()
                        if (isUrlValid(url)) {
                            urls.add(url)
                        }
                    }
                }
            }
        }
        return urls
    }

    /**
     * 解析相对URL为绝对URL
     */
    fun resolveUrl(baseUrl: String, relativeUrl: String): String {
        try {
            val base = UrlUtil.url(baseUrl)
            return UrlUtil.getURL(base, relativeUrl).toString()
        } catch (e: Exception) {
            return relativeUrl
        }
    }

    /**
     * 判断是否为页面链接（核心方法）
     */
    fun isPageLink(url: String): Boolean {
        if (StrUtil.isBlank(url)) {
            return false
        }
        // 1. 排除资源链接后缀
        if (RESOURCE_LINK_PATTERN.matcher(url).matches()) {
            return false
        }
        // 2. 匹配页面链接后缀 或 无后缀（如 /home、/article/123）
        if (PAGE_LINK_PATTERN.matcher(url).matches() || !url.contains(".")) {
            return true
        }
        // 3. 特殊场景：URL带参数但无后缀（如 /index?page=1）
        val urlWithoutParams = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return !urlWithoutParams.contains(".") || PAGE_LINK_PATTERN.matcher(urlWithoutParams).matches()
    }
}