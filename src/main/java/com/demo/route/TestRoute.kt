package com.demo.route

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


/**
 * @author theo
 * @date 2019/4/16
 */
@Controller
@RequestMapping("/demo", "omed")
class TestRoute {

    @RequestMapping("/test", "tset")
    fun test(id: String?): String {
        return "data +$id"
    }

}