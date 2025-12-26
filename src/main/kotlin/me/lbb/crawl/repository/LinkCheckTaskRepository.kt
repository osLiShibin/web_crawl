package me.lbb.crawl.repository

import me.lbb.crawl.entity.LinkCheckTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Description: JPA接口
 *
 * @author csy
 * Created on 2025/12/5 16:18.
 */
@Repository
interface LinkCheckTaskRepository : JpaRepository<LinkCheckTask, Long> {
}