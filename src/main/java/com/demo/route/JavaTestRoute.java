package com.demo.route;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author theo
 * @date 2019/4/22
 */
@RestController
@RequestMapping("/java")
public class JavaTestRoute {

    @RequestMapping("/demo")
    public String test(String id) throws InterruptedException {
        Thread.sleep(1000);
        return "data java " + id;
    }

}
