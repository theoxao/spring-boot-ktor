package com.demo.route

import kotlinx.coroutines.delay
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping


/**
 * @author theo
 * @date 2019/4/16
 */
@Controller
@RequestMapping("/demo", "omed")
class TestRoute {

    @RequestMapping("/test", "tset")
    suspend fun test(model: Model): String {
        delay(1000)
        println(Thread.currentThread().name)
        model.addAttribute("res", User(1, "theo"))
        return "index.ftl"
    }
}


data class User(private val id: Int, private val name: String)