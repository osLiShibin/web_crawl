package me.lbb.crawl.support

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Description: 请求频率限制器（控制并发+随机延时）
 *
 * @author csy
 * Created on 2025/12/6 11:39.
 */
object RequestRateLimiter {
    private val logger: Logger = LoggerFactory.getLogger("RequestRateLimiter")

    // 最大并发请求
    const val MAX_CONCURRENT_REQUESTS = 3;

    // 基础请求间隔(ms)，在此基础上随机浮动
    const val BASE_DELAY_MS = 500L

    // 随机浮动范围（ms）
    const val RANDOM_DELAY_RANGE = 500L

    val SEMAPHORE = Semaphore(MAX_CONCURRENT_REQUESTS)

    val random = Random.Default

    /**
     * 获取请求许可
     */
    fun acquire() {
        try {
            SEMAPHORE.acquire()
            // 随机延迟（基础间隔 + 0~RANDOM_DELAY_RANGE）
            val delay = BASE_DELAY_MS + random.nextLong(RANDOM_DELAY_RANGE)
            TimeUnit.MILLISECONDS.sleep(delay)
        } catch (e: InterruptedException) {
            logger.warn("请求频率控制被中断", e)
            Thread.currentThread().interrupt()
        }
    }

    /**
     * 释放请求许可
     */
    fun release() {
        SEMAPHORE.release()
    }

    /**
     * 随机User-Agent
     */
    fun getRandomUserAgent(): String {
        val userAgents = arrayOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Firefox/121.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        return userAgents[random.nextInt(userAgents.size)]
    }

    /**
     * 指数退避延迟（重试时使用）
     * @param retryCount 重试次数
     * @return 延迟时间（ms）
     */
    fun getExponentialBackoffDelay(retryCount: Int): Long {
        // 基础延迟 1s，指数递增
        val delay = (1000 * 2.toDouble().pow(retryCount.toDouble())).toLong()
        return min(delay, 30000)
    }
}