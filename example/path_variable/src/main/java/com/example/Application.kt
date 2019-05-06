package com.example

import kotlinx.coroutines.delay
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author theo
 * @date 2019/5/6
 */
@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}


@RestController
@RequestMapping("/")
class BarController {

    @RequestMapping("/foo/{bar}/{boo}")
    suspend fun foo(bar: String, boo: String): String {
        delay(1000)
        return bar + boo
    }
}