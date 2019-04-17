package com.demo.annotations

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*


/**
 * @author theo
 * @date 2019/4/16
 */
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Component
annotation class Controller {
}