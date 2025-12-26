package me.lbb.crawl.support

import org.apache.commons.lang3.StringUtils
import org.dromara.hutool.core.text.StrUtil
import org.openqa.selenium.By
import org.openqa.selenium.Cookie
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import us.codecraft.webmagic.Page
import us.codecraft.webmagic.Request
import us.codecraft.webmagic.Task
import us.codecraft.webmagic.downloader.AbstractDownloader
import us.codecraft.webmagic.selector.PlainText
import us.codecraft.webmagic.utils.HttpConstant
import java.io.Closeable
import java.io.IOException
import java.time.Duration
import kotlin.concurrent.Volatile


/**
 * Description: WebDriver
 *
 * @author csy
 * Created on 2025/9/25 15:24.
 */
class SeleniumDownloader : AbstractDownloader, Closeable {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Volatile
    var webDriverPool: WebDriverPool? = null
    private var sleepTime = 0
    private var poolSize = 1
    private val driver: String

    /**
     * 新建
     *
     * @param chromeDriverPath chromeDriverPath
     */
    constructor(driver: String, chromeDriverPath: String) {
        this.driver = driver
        if (StringUtils.isNotBlank(chromeDriverPath)) {
            System.getProperties().setProperty("webdriver.chrome.driver", chromeDriverPath)
        }
    }

    constructor(driver: String) {
        this.driver = driver
    }


    /**
     * set sleep time to wait until load success
     *
     * @param sleepTime sleepTime
     * @return this
     */
    fun setSleepTime(sleepTime: Int): SeleniumDownloader {
        this.sleepTime = sleepTime
        return this
    }

    override fun download(request: Request, task: Task): Page? {
        require(!StringUtils.isBlank(driver)) { "driver is empty" }
        checkInit()
        var webDriver: WebDriver? = null
        val page = Page.ofFailure(request)
        try {
            request.putExtra("startTime", System.currentTimeMillis())
            webDriver = webDriverPool!!.get(driver)
            webDriver.get(request.url)
            try {
                // 等待JS执行
                val wait = WebDriverWait(webDriver, Duration.ofSeconds(5))
                wait.until(ExpectedCondition { wd: WebDriver? -> (wd as JavascriptExecutor).executeScript("return document.readyState") == "complete" }) // 等待页面加载完成
                val statusCode =
                    (webDriver as JavascriptExecutor).executeScript("return window.performance.getEntries()[0].responseStatus;") // 获取当前URL的响应状态码（实际上是通过URL来判断，而非直接获取HTTP状态码）
                logger.debug("Status code: {}", statusCode)
                webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5))
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime.toLong())
                }
            } catch (e: InterruptedException) {
                logger.error(e.message, e)
            }
            val manage = webDriver.manage()
            val site = task.site
            if (site.cookies != null) {
                for (cookieEntry in site.cookies
                    .entries) {
                    val cookie = Cookie(
                        cookieEntry.key,
                        cookieEntry.value
                    )
                    manage.addCookie(cookie)
                }
            }

            val webElement = webDriver.findElement(By.xpath("/html"))
            val content = webElement.getAttribute("outerHTML")
            if (StrUtil.isNotBlank(content)) {
                page.setRawText(webDriver.pageSource)
                page.headers = mapOf("Content-Type" to listOf("text/html"))
            } else {
                page.setRawText(webDriver.pageSource)
            }
            page.isDownloadSuccess = true
            page.url = PlainText(request.url)
            page.setRequest(request)
            page.statusCode = HttpConstant.StatusCode.CODE_200
            onSuccess(page, task)
        } catch (e: Exception) {
            logger.error("download page {} error", request.url, e)
            page.statusCode = 500
            onError(page, task, e)
        } finally {
            if (webDriver != null) {
                webDriverPool!!.returnToPool(webDriver)
            }
        }
        return page
    }

    private fun checkInit() {
        if (webDriverPool == null) {
            synchronized(this) {
                webDriverPool = WebDriverPool(poolSize)
            }
        }
    }

    override fun setThread(thread: Int) {
        this.poolSize = thread
    }

    @Throws(IOException::class)
    override fun close() {
        webDriverPool!!.closeAll()
    }
}

