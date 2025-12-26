package me.lbb.crawl.support

import me.lbb.crawl.entity.LinkCheckResult
import me.lbb.crawl.repository.LinkCheckResultRepository
import org.apache.commons.collections4.CollectionUtils
import us.codecraft.webmagic.ResultItems
import us.codecraft.webmagic.Task
import us.codecraft.webmagic.pipeline.Pipeline

/**
 * Description: 管道
 *
 * @author csy
 * Created on 2025/12/5 17:36.
 */
class LinkCheckPipeline(
    private val linkCheckResultRepository: LinkCheckResultRepository
) : Pipeline {
    override fun process(resultItems: ResultItems, task: Task) {
        val result = resultItems.get<LinkCheckResult>("result")
        val checked = resultItems.get<List<LinkCheckResult>>("checkedList")
        if (result != null) {
            linkCheckResultRepository.save(result)
        }
        if (CollectionUtils.isNotEmpty(checked)) {
            linkCheckResultRepository.saveAll(checked)
        }
    }
}