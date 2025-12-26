package me.lbb.crawl.support

import org.dromara.hutool.core.net.ssl.SSLContextUtil
import org.dromara.hutool.core.net.ssl.TrustAnyHostnameVerifier
import org.dromara.hutool.core.net.url.UrlUtil
import org.dromara.hutool.http.client.engine.jdk.HttpUrlConnectionUtil
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

/**
 * Description: 检查链接
 *
 * @author csy
 * Created on 2025/12/6 11:22.
 */
class LinkValidator(
    private val timeout: Int,
) {

    private val logger = LoggerFactory.getLogger(LinkValidator::class.java)
    val verifier = TrustAnyHostnameVerifier()
    val sslContext = SSLContextUtil.createTrustAnySSLContext()

    companion object {
        const val MAX_RETRY = 2
    }

    fun validate(url: String): ValidationResult {
        val startTime = System.currentTimeMillis()
        val result = ValidationResult()
        result.url = url

        RequestRateLimiter.acquire()
        try {
            val openHttp = HttpUrlConnectionUtil.openHttp(UrlUtil.url(url), null)
            openHttp.requestMethod = "HEAD" // 优先使用HEAD方法
            openHttp.connectTimeout = timeout
            openHttp.readTimeout = timeout
            openHttp.setRequestProperty("User-Agent", RequestRateLimiter.getRandomUserAgent())
            openHttp.instanceFollowRedirects = true

            if (openHttp is HttpsURLConnection) {
                openHttp.hostnameVerifier = TrustAnyHostnameVerifier()
                openHttp.sslSocketFactory = sslContext.socketFactory
            }

            // 连接并获取响应码
            openHttp.connect()
            val statusCode = openHttp.responseCode
            result.statusCode = statusCode
            result.valid = statusCode >= 200 && statusCode < 400

            openHttp.disconnect()

            if (result.statusCode == 403) {
                retry(url, result)
            }
        } catch (e: IOException) {
            logger.error(e.message, e)
            retry(url, result)
        } catch (e: Exception) {
            logger.error(e.message, e)
            result.valid = false
            result.statusCode = -1
            result.errorMessage = e.message
        } finally {
            RequestRateLimiter.release()
        }

        val duration = System.currentTimeMillis() - startTime
        result.duration = duration
        return result
    }

    private fun retry(url: String, result: ValidationResult) {
        var retryCount = 1
        while (retryCount < MAX_RETRY) {
            val delay = RequestRateLimiter.getExponentialBackoffDelay(retryCount + 1)
            TimeUnit.MILLISECONDS.sleep(delay)
            try {
                validateWithGet(url, result)
                break
            } catch (ex: Exception) {
                logger.error(ex.message, ex)
                result.valid = false
                result.statusCode = -1
                result.errorMessage = ex.message
                retryCount++

            }
        }
    }

    private fun validateWithGet(url: String, result: ValidationResult) {
        val openHttp = HttpUrlConnectionUtil.openHttp(UrlUtil.url(url), null)
        openHttp.requestMethod = "GET" // 请求降级为GET
        openHttp.connectTimeout = timeout
        openHttp.readTimeout = timeout
        openHttp.setRequestProperty("User-Agent", RequestRateLimiter.getRandomUserAgent())
        openHttp.instanceFollowRedirects = true
        if (openHttp is HttpsURLConnection) {
            openHttp.hostnameVerifier = verifier
            openHttp.sslSocketFactory = sslContext.socketFactory
        }
        // 连接并获取响应码
        openHttp.connect()
        val statusCode = openHttp.responseCode
        result.statusCode = statusCode
        result.valid = statusCode >= 200 && statusCode < 400
        openHttp.disconnect()
    }
}