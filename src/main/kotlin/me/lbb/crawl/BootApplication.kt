package me.lbb.crawl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Description: 启动类
 *
 * @author csy
 * Created on 2025/12/5 16:17.
 */
@SpringBootApplication
class BootApplication

fun main(args: Array<String>) {
    runApplication<BootApplication>(*args)
}