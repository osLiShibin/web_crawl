package me.lbb.crawl.support

/**
 * Description: 校验结果
 *
 * @author csy
 * Created on 2025/12/6 11:34.
 */
class ValidationResult {
    var url: String? = null
    var valid: Boolean = false
    var statusCode: Int? = null
    var errorMessage: String? = null
    var duration: Long? = null

    override fun toString(): String {
        return "ValidationResult{url=${url}, valid=$valid, duration=$duration, statusCode=$statusCode, errorMessage=$errorMessage}"
    }
}