package com.demo

import io.ktor.application.Application
import io.ktor.application.ApplicationEnvironment
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.content.*
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

/**
 * @author theo
 * @date 2019/4/16
 */
@SpringBootApplication(exclude= [JdbcTemplateAutoConfiguration::class , HibernateJpaAutoConfiguration::class])
open class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<com.demo.Application>(*args)
        }
    }
}


