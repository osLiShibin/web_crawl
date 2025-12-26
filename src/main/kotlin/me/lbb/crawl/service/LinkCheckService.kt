package me.lbb.crawl.service

import me.lbb.crawl.entity.LinkCheckTask

/**
 * Description: 业务
 *
 * @author csy
 * Created on 2025/12/8 11:44.
 */
interface LinkCheckService {

    /**
     * 启动任务
     */
    fun startLinkCheckTask(taskId: Long, siteUrl: String, excludePatterns: List<String>, xpath: String)

    /**
     * 创建任务
     */
    fun createAndRunTask(name: String, config: String): LinkCheckTask
}