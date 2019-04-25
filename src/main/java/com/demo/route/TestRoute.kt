package com.demo.route

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


/**
 * @author theo
 * @date 2019/4/16
 */
@RestController
@RequestMapping("/demo", "omed")
class TestRoute {

    @RequestMapping(value = ["/test", "tset"], method = [RequestMethod.GET])

    suspend fun test(@RequestBody user: User, age: List<Int>?, model: Model): User {
//        delay(1000)
//        println(Thread.currentThread().name)
//        model.addAttribute("res", User(1, "theo"))
        user.age = age
        return user
    }
}

data class User(var id: Int, var name: String, var age: List<Int>?)