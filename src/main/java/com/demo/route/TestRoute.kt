package com.demo.route

import com.demo.common.RestResponse
import kotlinx.coroutines.delay
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException


/**
 * @author theo
 * @date 2019/4/16
 */
@Controller
@RequestMapping("/demo", "omed")
class TestRoute {

    @RequestMapping("/test", "tset")
    suspend fun test(id: String): String {
        delay(1000)
        println(Thread.currentThread().name)
        return "redirect:$id"
    }
}