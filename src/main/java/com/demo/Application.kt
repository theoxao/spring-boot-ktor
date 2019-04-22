package com.demo

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author theo
 * @date 2019/4/16
 */
@SpringBootApplication(exclude= [JdbcTemplateAutoConfiguration::class , HibernateJpaAutoConfiguration::class])
open class Application {
    companion object {

        private val list = ConcurrentHashMap<String, Pair<PipelineContext<Unit, ApplicationCall>, Deferred<Unit>>>()

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<com.demo.Application>(*args)
//            Thread.sleep(100000000)
//            embeddedServer(Netty, 8888) {
//                routing {
//                    get("/test") {
//                        val await = GlobalScope.async {
//                            repeat(100000) {
//                                delay(5000)
//                                println("waiting")
//                            }
//                        }
//                        list.put( Thread.currentThread().name,Pair(this, await))
//                        await.await()
//                    }
//                    post("/test") {
//                        list.forEachValue(5) {
//                            GlobalScope.launch {
//                                it.first.context.respond("server send message")
//                                it.first.finish()
//                                it.second.cancel()
//                            }
//                        }
//                        list.clear()
//                    }
//                }
//            }.start(wait = true)
        }
    }
}


