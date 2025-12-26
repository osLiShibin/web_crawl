package me.lbb.crawl.support

import us.codecraft.webmagic.Request
import us.codecraft.webmagic.SpiderListener
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description: 监听
 *
 * @author csy
 * Created on 2025/12/8 8:15.
 */
class LinkCheckListener : SpiderListener {

    val totalCount = AtomicInteger(0)
    val failedCount = AtomicInteger(0)

    override fun onSuccess(request: Request?) {
        println("Successfully link checked: ${System.currentTimeMillis()}")
        totalCount.incrementAndGet()
        // 从Request中获取PageProcessor存入的响应状态码
        val statusCode = request?.getExtra<Int>("statusCode")
        // 仅 200-399 视为有效链接，其余（404/500等）标记为无效
        if (statusCode == null || statusCode < 200 || statusCode >= 400) {
            failedCount.incrementAndGet();
        }
    }

    override fun onError(request: Request?, e: Exception?) {
        println("Monitor Failure: ${e?.message}: ")
        failedCount.incrementAndGet()
        super.onError(request, e)
    }
}