package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


/**
 * @author theo
 * @date 2019/5/5
 */
@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Controller
@RequestMapping("/")
class FooController{
    @RequestMapping("/foo")
    fun static(): String {
        return "static:bar.txt"
    }
}
