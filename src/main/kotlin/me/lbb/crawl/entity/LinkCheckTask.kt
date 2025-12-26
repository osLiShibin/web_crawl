package me.lbb.crawl.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType


/**
 * Description: 任务
 *
 * @author csy
 * Created on 2025/12/5 16:11.
 */
@Entity
@Table(name = "link_check_task")
class LinkCheckTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var taskName: String? = null

    var startTime: LocalDateTime? = null

    var endTime: LocalDateTime? = null

    // RUNNING, SUCCESS, FAILED
    var status: String? = null

    var totalLinks = 0

    var invalidLinks = 0

    @Column(columnDefinition = "TEXT")
    var config: String? = null // Store JSON config if needed
}