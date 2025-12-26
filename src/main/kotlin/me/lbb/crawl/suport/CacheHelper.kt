package me.lbb.crawl.support

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.lbb.crawl.entity.LinkCheckResult
import java.util.concurrent.TimeUnit

/**
 * Description: 缓存
 *
 * @author csy
 * Created on 2025/12/6 14:36.
 */
object CacheHelper {
    
    // 分组缓存容器
    private val container = CacheBuilder.newBuilder()
        .maximumSize(10)
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build<Long, Cache<String, LinkCheckResult>>()
    
    // 全局
    private val cache = CacheBuilder.newBuilder()
        .maximumSize(10000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build<String, LinkCheckResult>()

    fun get(url: String): LinkCheckResult? {
        return cache.getIfPresent(url)
    }

    fun put(url: String, response: LinkCheckResult) {
        cache.put(url, response)
    }

    fun clear() {
        cache.invalidateAll()
    }

    fun createCache(id: Long) {
        if (container.getIfPresent(id) == null) {
            container.put(id, CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build<String, LinkCheckResult>())
        }
    }

    fun getCache(id: Long): Cache<String, LinkCheckResult> {
        return container.get(id, {CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build<String, LinkCheckResult>()})
    }

    fun get(id: Long, url: String): LinkCheckResult? {
        return getCache(id).getIfPresent(url)
    }

    fun put(id: Long, url: String, response: LinkCheckResult) {
        getCache(id).put(url, response)
    }

    fun clear(id: Long) {
        getCache(id).invalidateAll()
    }
}