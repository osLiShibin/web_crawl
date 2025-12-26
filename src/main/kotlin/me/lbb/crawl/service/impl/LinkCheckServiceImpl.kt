package me.lbb.crawl.service.impl

import me.lbb.crawl.entity.LinkCheckTask
import me.lbb.crawl.repository.LinkCheckResultRepository
import me.lbb.crawl.repository.LinkCheckTaskRepository
import me.lbb.crawl.service.LinkCheckService
import me.lbb.crawl.support.CacheHelper
import me.lbb.crawl.support.EnhanceHttpClientDownloader
import me.lbb.crawl.support.LinkCheckListener
import me.lbb.crawl.support.LinkCheckPageProcessor
import me.lbb.crawl.support.LinkCheckPipeline
import me.lbb.crawl.support.SeleniumDownloader
import org.dromara.hutool.core.text.StrUtil
import org.dromara.hutool.extra.spring.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.Assert
import us.codecraft.webmagic.Request
import us.codecraft.webmagic.Spider
import us.codecraft.webmagic.downloader.HttpClientDownloader
import java.time.LocalDateTime

/**
 * Description: 业务接口
 *
 * @author libinbin
 * Created on 2025/12/5 16:21.
 */
@Service
open class LinkCheckServiceImpl(
    private val taskRepository: LinkCheckTaskRepository,
    private val linkCheckResultRepository: LinkCheckResultRepository,
    private val transactionTemplate: TransactionTemplate
) : LinkCheckService, ApplicationRunner, DisposableBean {
    private val logger = LoggerFactory.getLogger(LinkCheckServiceImpl::class.java)

    private val downloader = EnhanceHttpClientDownloader(
        HttpClientDownloader(),
        SeleniumDownloader("firefox").setSleepTime(2000)
    )

    @Async
    override fun startLinkCheckTask(taskId: Long, siteUrl: String, excludePatterns: List<String>, xpath: String) {
        val task = taskRepository.findById(taskId).orElse(null)
        if (task == null) {
            return
        }

        val linkCheckListener = LinkCheckListener()
        try {
            // 清除缓存
            CacheHelper.clear(task.id!!)
            val request = Request(siteUrl)
            request.extras = mutableMapOf("depth" to 1, "isHtml" to true)
            val spider = Spider.create(LinkCheckPageProcessor(listOf(), siteUrl, taskId, ""))
                .addRequest(request)
                .addPipeline(LinkCheckPipeline(linkCheckResultRepository))
                .thread(4)
                .setSpiderListeners(listOf(linkCheckListener))
                .setDownloader(downloader) // PhantomJSDownloader("C:\\tools\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe")
            spider.run() // 启动爬虫（阻塞直到完成）

            task.status = "SUCCESS"
        } catch (e: Exception) {
            logger.error(e.message, e)
            task.status = "FAILED"
        } finally {
            task.totalLinks = linkCheckListener.totalCount.get()
            task.invalidLinks = linkCheckListener.failedCount.get()
            task.endTime = LocalDateTime.now()
            transactionTemplate.execute {
                taskRepository.save(task)
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun createAndRunTask(name: String, config: String): LinkCheckTask {
        val task = LinkCheckTask()
        task.taskName = name
        task.config = config
        task.startTime = LocalDateTime.now()
        task.status = "RUNNING"
        return taskRepository.save(task)
    }

    override fun run(args: ApplicationArguments?) {
        var path = SpringUtil.getProperty("crawl.chromedriver.path")
        if (StrUtil.isNotEmpty(path)) {
            System.getProperties().setProperty("webdriver.chrome.driver", path)
        }
        path = SpringUtil.getProperty("crawl.geckodriver.path")
        if (StrUtil.isNotEmpty(path)) {
            System.getProperties().setProperty("webdriver.firefox.driver", path)
        }
        Assert.notNull(path, "driver path cannot be null")
    }

    override fun destroy() {
        downloader.seleniumDownloader.webDriverPool?.destroy()
    }
}