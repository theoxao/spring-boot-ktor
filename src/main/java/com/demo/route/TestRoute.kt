package com.demo.route

import kotlinx.coroutines.delay
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * @author theo
 * @date 2019/4/16
 */
@RestController
@RequestMapping("/demo", "omed")
class TestRoute {

    @RequestMapping("/test", "tset")

    suspend fun test(@RequestBody user: User): User {
//        delay(1000)
//        println(Thread.currentThread().name)
//        model.addAttribute("res", User(1, "theo"))
        return user
    }
}


data class User(private val id: Int, private val name: String)