package me.lbb.crawl.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType


/**
 * Description: 链接检查结果
 *
 * @author csy
 * Created on 2025/12/5 16:16.
 */
@Entity
@Table(name = "link_check_result")
class LinkCheckResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var taskId: Long? = null

    @Column(columnDefinition = "TEXT")
    var url: String? = null

    @Column(columnDefinition = "TEXT")
    var parentUrl: String? = null

    // PAGE, RESOURCE
    var type: String? = null

    var isExternal: Boolean? = null

    var responseStatus: Int? = null

    @Column(columnDefinition = "TEXT")
    var extractedData: String? = null

    var isValid: Boolean? = null

    var checkTime: LocalDateTime? = null

    var checkDurationMs: Long? = null
}