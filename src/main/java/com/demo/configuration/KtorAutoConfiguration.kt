package com.demo.configuration

import com.demo.Application
import com.demo.common.RestResponse
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EngineAPI
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.LocalVariableTableParameterNameDiscoverer
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.Assert
import org.springframework.validation.support.BindingAwareConcurrentModel
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestMethod.*
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Method
import java.util.*
import javax.annotation.Resource
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.reflect.jvm.kotlinFunction


/**
 * @author theo
 * @date 2019/4/16
 */
@Configuration
@EnableConfigurationProperties(KtorProperties::class)
open class KtorAutoConfiguration {

    @Resource
    private lateinit var properties: KtorProperties

    @EngineAPI
    @KtorExperimentalAPI
    @Bean
    open fun applicationEngine(context: ApplicationContext): ApplicationEngine {
        val engineFactory = when (properties.engine) {
            "CIO" -> CIO
            else -> Netty
        }

        return embeddedServer(engineFactory, properties.port, properties.host) {
            install(ContentNegotiation) {
                jackson { }
            }
            if (Class.forName("io.ktor.freemarker.FreeMarker") != null)
                install(FreeMarker) {
                    templateLoader = ClassTemplateLoader(Application::class.java, "/${properties.templatesRoot}")
                }

            val beans = context.getBeansWithAnnotation(Controller::class.java).values

            val allDefinitions = beans.flatMap { bean ->
                val mappingAnnotation = bean.javaClass.getDeclaredAnnotation(RequestMapping::class.java)
                bean.javaClass.methods.filter {
                    it.isAnnotationPresent(RequestMapping::class.java)
                }.map {
                    val list = it.getDeclaredAnnotation(RequestMapping::class.java).value.flatMap { child ->
                        mappingAnnotation.value.map { parent ->
                            parent + if (child.startsWith("/")) child else "/$child"
                        }
                    }
                    RouteDefinition(it, bean, mappingAnnotation.method, list)
                }
            }
            routing {
                if (properties.enableTrace) {
                    trace {
                        println((it.buildText()))
                    }
                }
                install(StatusPages) {
                    exception<Throwable> { e ->
                        call.respond(HttpStatusCode.InternalServerError, RestResponse.error<String>(e.localizedMessage
                                ?: "Unknown Exception"))
                    }
                }
                static("/${properties.staticRoot}") {
                    resources("static/")
                }
                route("/") {
                    allDefinitions.forEach {
                        mapping(it)
                    }
                }
            }
        }.start(wait = false)
    }

    private val discoverer = LocalVariableTableParameterNameDiscoverer()

    private fun Route.mapping(definition: RouteDefinition) {
        val method = definition.method
        val bean = definition.bean
        if (definition.methods.isEmpty()) {
            definition.methods = values()
        }
        definition.methods.forEach { requestMethod ->
            definition.uri.forEach { uri ->
                when (requestMethod) {
                    GET -> {
                        get(uri) {
                            //FIXME too many reflections
                            val params = call.parameters
                            val param = discoverer.getParameterNames(method)?.filter(Objects::nonNull)?.map {
                                params[it]
                            }?.toTypedArray()!!
                            val model = BindingAwareConcurrentModel()
                            val message = method.invokeSuspend(bean, param)
                            handleView(message, definition, model)
                        }
                    }
                    POST -> {
                        post(uri) {
                            val param = arrayOf("1")
                            val message = method.invokeSuspend(bean, param)
                        }
                    }
//                HEAD -> TODO()
//                PUT -> TODO()
//                PATCH -> TODO()
//                DELETE -> TODO()
//                OPTIONS -> TODO()
//                TRACE -> TODO()
                    else -> {
                    }
                }
            }
        }
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleView(result: Any?, definition: RouteDefinition, model: Model) {
        if (result == null || result == Unit)
            call.respond("")
        else {
            if (definition.bean.javaClass.getAnnotation(ResponseBody::class.java) != null || definition.bean.javaClass.getAnnotation(RestController::class.java) != null || definition.method.getAnnotation(ResponseBody::class.java) != null)
                call.respond(result)
            Assert.isTrue(result is String, "unable to handle result (${result.javaClass.typeName}) without responseBody")
            val resultStr = result as String
            if (resultStr.startsWith("redirect:"))
                call.respondRedirect(resultStr.removePrefix("redirect:"), true)
            else if (resultStr.startsWith("static:"))
                call.respondRedirect("/${properties.staticRoot}/${resultStr.removePrefix("static:")}")
            else
                call.respond(FreeMarkerContent(result, model.asMap()))
        }
    }
}

@ConfigurationProperties(prefix = "spring.ktor")
open class KtorProperties {
    open var host: String = "0.0.0.0"
    open var port: Int = 8088
    open var enableTrace = false
    open var engine = "Netty"
    open var staticRoot = "static"
    open val templatesRoot = "templates"
}


data class RouteDefinition(val method: Method, val bean: Any, var methods: Array<RequestMethod>, val uri: List<String>)

/**
 * invoke method
 * if its suspend, set continuation
 */
suspend fun Method.invokeSuspend(obj: Any, args: Array<*>): Any? =
        if (this.kotlinFunction != null && this.kotlinFunction!!.isSuspend) {
            suspendCoroutineUninterceptedOrReturn<Any> {
                //add continuation at the end of args
                val list = arrayListOf(*args)
                list[list.size - 1] = it
                invoke(obj, *list.toArray())
            }
        } else {
            invoke(obj, *args)
        }