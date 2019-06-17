package com.theoxao

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

/**
 * @author theo
 * @date 2019/4/16
 */
@SpringBootApplication(exclude = [JdbcTemplateAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

