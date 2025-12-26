package me.lbb.crawl.support

import org.dromara.hutool.core.text.StrUtil
import org.dromara.hutool.core.util.RuntimeUtil
import org.dromara.hutool.core.util.SystemUtil
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger


/**
 * Description: 驱动
 *
 * @author csy
 * Created on 2025/9/25 15:21.
 */
class WebDriverPool @JvmOverloads constructor(private val capacity: Int = DEFAULT_CAPACITY) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val stat = AtomicInteger(STAT_RUNNING)

    /**
     * store webDrivers created
     */
    private val webDriverList: MutableList<WebDriver> =
        Collections.synchronizedList(ArrayList())

    /**
     * store webDrivers available
     */
    private val innerQueue: BlockingDeque<WebDriver> = LinkedBlockingDeque()

    @Throws(IOException::class)
    fun configure(driver: String): WebDriver {
        // Start appropriate Driver
        if (driver == DRIVER_FIREFOX) {
            return FirefoxDriver(this.firefoxOptions)
        } else if (driver == DRIVER_CHROME) {
            return ChromeDriver(this.chromeOptions)
        } else {
            throw IllegalArgumentException("Invalid driver: " + driver)
        }
    }

    private val chromeOptions: ChromeOptions
        get() {
            val options = ChromeOptions()
            options.addArguments("--disable-logging")
            options.addArguments("--ignore-certificate-errors")
            // 字符编码 utf-8 支持中文字符
            options.addArguments("lang=zh_CN.UTF-8")
            // 设置容许弹框
            options.addArguments("disable-infobars", "disable-web-security")
            // 驱动自动控制软件标识
            options.addArguments("--disable-blink-features=AutomationControlled")
            options.setExperimentalOption("excludeSwitches", arrayOf<String>("enable-automation"))
            // 设置无gui 开发时仍是不要加，能够看到浏览器效果
            options.addArguments("--headless")
            options.addArguments("--disable-gpu") //禁止gpu渲染
            options.addArguments("--no-sandbox") //关闭沙盒模式
            options.addArguments("--disable-dev-shm-usage")
            options.addArguments("--incognito") // 隐身模式（无痕模式）
            options.addArguments("--disable-extensions") // disabling extensions
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL)
            //禁用日志
            options.addArguments("--log-level=3")
            options.addArguments("--silent")

            val prefs = HashMap<String?, Any?>()
            prefs.put("profile.default_content_settings", 2)
            options.setExperimentalOption("prefs", prefs)
            options.addArguments("blink-settings=imagesEnabled=false") //禁用图片
            return options
        }

    private val firefoxOptions: FirefoxOptions
        get() {
            val options = FirefoxOptions()
            options.addArguments("--headless")
            val profile = FirefoxProfile()
            profile.setAcceptUntrustedCertificates(true)
            options.setProfile(profile)
            options.setPageLoadStrategy(PageLoadStrategy.EAGER)
            options.addPreference("permissions.default.image", 2)
            return options
        }

    /**
     * @return
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
    fun get(driver: String): WebDriver {
        checkRunning()
        val poll = innerQueue.poll()
        if (poll != null) {
            return poll
        }
        if (webDriverList.size < capacity) {
            synchronized(webDriverList) {
                if (webDriverList.size < capacity) {
                    // add new WebDriver instance into pool

                    try {
                        val webDriver = configure(driver)
                        innerQueue.add(webDriver)
                        webDriverList.add(webDriver)
                    } catch (e: IOException) {
                        logger.error(e.message, e)
                    }
                }
            }
        }
        return innerQueue.take()
    }

    fun returnToPool(webDriver: WebDriver) {
        checkRunning()
        innerQueue.add(webDriver)
    }

    protected fun checkRunning() {
        check(stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) { "Already closed!" }
    }

    fun closeAll() {
        val b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED)
        check(b) { "Already closed!" }
        for (webDriver in webDriverList) {
            val webDriver: WebDriver? = webDriver
            logger.info("Quit webDriver" + webDriver)
            webDriver?.quit()
        }
    }

    companion object {
        private const val DEFAULT_CAPACITY = 5
        private const val STAT_RUNNING = 1
        private const val STAT_CLODED = 2
        private const val DRIVER_FIREFOX = "firefox"
        private const val DRIVER_CHROME = "chrome"
    }

    fun destroy() {
        //根据不同的操作系统结束残留的chrome进程
        val osName: String = SystemUtil.get("os.name")
        if (StrUtil.isNotEmpty(osName)) {
            val commands = getCommand(osName)
            if (!commands.isEmpty()) {
                try {
                    RuntimeUtil.exec(commands.toTypedArray())
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    private fun getCommand(osName: String): MutableList<String> {
        val commands: MutableList<String> = ArrayList<String>()
        if (osName.lowercase().startsWith("windows")) {
            commands.add("taskkill /F /im geckodriver.exe")
            commands.add("taskkill /F /im chromedriver.exe")
        } else if (osName.lowercase().startsWith("linux")) {
            commands.add("ps -ef | grep chromedriver | grep -v grep  | awk '{print \"kill -9 \"$2}'  | sh")
            commands.add("ps -ef | grep geckodriver | grep -v grep  | awk '{print \"kill -9 \"$2}'  | sh")
        }
        return commands
    }
}
