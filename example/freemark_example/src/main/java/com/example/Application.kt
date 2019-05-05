package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
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
class BarController {

    @RequestMapping("foo")
    fun foo(model: Model): String {
        model.addAttribute("foo", "I am foo")
        val bar = mapOf("foo" to "I am foo too")
        model.addAttribute("bar", bar)
        return "index.ftl"
    }
}


