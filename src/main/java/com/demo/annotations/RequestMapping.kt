package com.demo.annotations

import com.demo.configuration.HttpMethod


/**
 * @author theo
 * @date 2019/4/16
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestMapping(val value: String = "", val method: HttpMethod = HttpMethod.GET)