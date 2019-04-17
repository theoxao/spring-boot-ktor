package com.demo.route

import com.demo.annotations.Controller
import com.demo.annotations.RequestMapping
import com.demo.configuration.HttpMethod


/**
 * @author theo
 * @date 2019/4/16
 */
@Controller
@RequestMapping("/demo")
class TestRoute {

    @RequestMapping("/test")
    fun test(id: String?): String {
        return "data +$id"
    }

}