package me.lbb.crawl.support

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Request
import us.codecraft.webmagic.Task
import us.codecraft.webmagic.downloader.AbstractDownloader
import us.codecraft.webmagic.downloader.HttpClientDownloader

/**
 * Description: 自定义
 *
 * @author csy
 * Created on 2025/12/8 14:59.
 */
class EnhanceHttpClientDownloader(
    private val httpClientDownloader: HttpClientDownloader,
    val seleniumDownloader: SeleniumDownloader
) : AbstractDownloader() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun download(request: Request, task: Task): Page? {
        val isHtml = request.getExtra<Boolean>("isHtml") ?: false
        request.putExtra("startTime", System.currentTimeMillis())
        var page: Page? = null
        try {
            page = if (!isHtml) {
                httpClientDownloader.download(request, task)
            } else {
                seleniumDownloader.download(request, task)
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            page = Page.ofFailure(request).apply {
                statusCode = 500
            }
        } finally {
            request.putExtra("endTime", System.currentTimeMillis())
            request.putExtra("statusCode", page?.statusCode)
        }
        return page
    }

    override fun setThread(threadNum: Int) {
        seleniumDownloader.setThread(threadNum)
        httpClientDownloader.setThread(threadNum)
    }
}