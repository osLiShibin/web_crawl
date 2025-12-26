package me.lbb.crawl.controller

import me.lbb.crawl.service.LinkCheckService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Description: 管理
 *
 * @author csy
 * Created on 2025/12/6 15:07.
 */
@RestController
@RequestMapping("/api/crawl")
class LinkCheckController(
    private val linkCheckService: LinkCheckService
) {

    @PostMapping("execute")
    fun execute(
        @RequestParam("name") name: String,
        @RequestParam("url") url: String
    ) {
        val task = linkCheckService.createAndRunTask(name, "")
        linkCheckService.startLinkCheckTask(
            task.id!!,
            url,
            listOf(),
            ""
        )
    }
}