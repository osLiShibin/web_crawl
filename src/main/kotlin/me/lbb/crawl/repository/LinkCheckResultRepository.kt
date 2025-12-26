package me.lbb.crawl.repository
import org.springframework.data.jpa.repository.JpaRepository
import me.lbb.crawl.entity.LinkCheckResult

/**
 * Description: JPA接口
 *
 * @author csy
 * Created on 2025/12/5 16:20.
 */
interface LinkCheckResultRepository : JpaRepository<LinkCheckResult, Long> {
}