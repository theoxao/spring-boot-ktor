package com.demo.configuration

import com.demo.annotations.Controller
import com.demo.annotations.RequestMapping
import  com.demo.configuration.HttpMethod.*
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.param
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.Resource


/**
 * @author theo
 * @date 2019/4/16
 */
@Configuration
@EnableConfigurationProperties(KtorProperties::class)
open class KtorAutoConfiguration {

    @Resource
    private lateinit var properties: KtorProperties

    /**
     * 注册引擎
     * @param engineFactory 依赖引擎工厂
     */
    @KtorExperimentalAPI
    @Bean
    open fun applicationEngine(context: ApplicationContext): ApplicationEngine {
        return embeddedServer(Netty, properties.port, properties.host) {
            install(ContentNegotiation) {
                jackson {

                }
            }
            val beans = context.getBeansWithAnnotation(Controller::class.java).values
            beans.forEach { bean ->
                var route = ""
                val crm = bean.javaClass.getAnnotation(RequestMapping::class.java)
                crm?.let {
                    route += it.value
                }
                bean.javaClass.methods.forEach { method ->
                    if (method.isAnnotationPresent(RequestMapping::class.java)) {
                        val rm = method.getAnnotation(RequestMapping::class.java)
                        routing {
                            val uri = route + rm.value
                            when (rm.method) {
                                GET -> {
                                    get(uri) {
                                        val param = call.parameters
                                        val list = method.parameters.map {
                                            param.getOrFail(it.name)
                                        }
                                        call.respond(method.invoke(bean, list.toTypedArray()))
                                    }
                                }
                                POST -> {
                                    post(uri) {
                                        call.respond(method.invoke(bean))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.start()
    }
}

@ConfigurationProperties(prefix = "spring.ktor")
open class KtorProperties {
    open var host: String = "0.0.0.0"
    open var port: Int = 8080
}
