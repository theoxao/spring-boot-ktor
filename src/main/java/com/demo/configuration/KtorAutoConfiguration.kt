@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.demo.configuration

import com.demo.Application
import com.demo.common.RestResponse
import com.demo.util.GsonUtil
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.locations.locations
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import io.ktor.request.receiveOrNull
import io.ktor.request.receiveParameters
import io.ktor.response.ApplicationResponse
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EngineAPI
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.Sessions
import io.ktor.sessions.sessions
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
import org.springframework.util.ReflectionUtils
import org.springframework.validation.support.BindingAwareConcurrentModel
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.math.BigDecimal
import java.math.BigInteger
import javax.annotation.Resource
import kotlin.coroutines.Continuation
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

    @KtorExperimentalLocationsAPI
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
                jackson {

                }
            }
            install(Locations)
            install(Sessions)
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
                        e.printStackTrace()
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

    @KtorExperimentalLocationsAPI
    private fun Route.mapping(definition: RouteDefinition) {
        val method = definition.method
        val bean = definition.bean
        if (definition.methods.isEmpty()) {
            definition.methods = values()
        }
        definition.methods.forEach { requestMethod ->
            definition.uri.forEach { uri ->
                val methodParams =
                        discoverer.getParameterNames(method)?.let { paramNames ->
                            method.parameters.mapIndexed { index, it ->
                                val param = Param(paramNames[index], it.type, null, it)
                                param
                            }
                        } ?: arrayListOf()
                val hasRequestBody = methodParams.map { it.fromRequestBody }.reduce { acc, b -> acc || b }
                val hasRequestHeader = methodParams.map { it.fromRequestHead }.reduce { acc, b -> acc || b }
                val hasSession = methodParams.map { it.fromSession }.reduce { acc, b -> acc || b }
                val hasCookie = methodParams.map { it.fromCookie }.reduce { acc, b -> acc || b }
                when (requestMethod) {
                    GET -> {
                        post(uri) {
                            val model by lazy { BindingAwareConcurrentModel() }
//                            val requestParam by lazy { call.parameters }
                            methodParams.forEachIndexed { index, param ->
                                param.value = when (param.methodParam.type) {
                                    Model::class.java -> model
                                    ApplicationRequest::class.java -> call.request
                                    ApplicationResponse::class.java -> call.response
                                    Continuation::class.java -> null
                                    else -> when {
                                        param.fromRequestBody -> {
                                            call.receiveOrNull(param.type.kotlin)
                                        }
                                        param.fromRequestHead -> {
                                            val rh = (param.methodParam.getAnnotation(RequestHeader::class.java))
                                            call.request.header(rh.name) ?: call.request.header(param.name)
                                            ?: rh.defaultValue.parse(param.type)
                                        }
                                        param.fromSession -> {
                                            val sa = (param.methodParam.getAnnotation(SessionAttribute::class.java))
                                            call.sessions.get(sa.name) ?: call.sessions.get(param.name)
                                        }
                                        param.fromCookie -> {
                                            val kv = (param.methodParam.getAnnotation(CookieValue::class.java))
                                            call.request.cookies[kv.name] ?: call.request.cookies[param.name]
                                            ?: kv.defaultValue.parse(param.type)
                                        }
                                        param.isSimpleClass -> {
                                            call.parameters[param.name]?.parse(param.type)
                                        }
                                        else -> {
                                            call.locations.resolve(param.type.kotlin, call)
                                        }
                                    }
                                }
                            }
                            val message = method.invokeSuspend(bean, methodParams.map { it.value }.toTypedArray())
                            handleView(message, definition, model)
                        }
                    }
                    POST -> {
                        post(uri) {
                            val parameters = call.receiveParameters()
                            method.parameters.filterIndexed { index, parameter ->
                                parameter.isAnnotationPresent(RequestBody::class.java)
                            }
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

    private suspend fun PipelineContext<Unit, ApplicationCall>.handleView(result: Any?, definition: RouteDefinition, model: Model?) {
        if (result == null || result == Unit)
            call.respond("")
        else {
            if (definition.bean.javaClass.getAnnotation(ResponseBody::class.java) != null || definition.bean.javaClass.getAnnotation(RestController::class.java) != null || definition.method.getAnnotation(ResponseBody::class.java) != null) {
                call.respond(result)
                return
            }
//            Assert.isTrue(result is String, "unable to handle result (${result.javaClass.typeName}) without responseBody")
            val resultStr = result as String
            when {
                resultStr.startsWith("redirect:") -> call.respondRedirect(resultStr.removePrefix("redirect:"), true)
                resultStr.startsWith("static:") -> call.respondRedirect("/${properties.staticRoot}/${resultStr.removePrefix("static:")}")
                else -> call.respond(FreeMarkerContent(result, GsonUtil.toMap(GsonUtil.toJson(model!!.asMap()))))
            }
        }
    }
}

inline fun <reified T> T.fromParam(parameters: Parameters): T {
    ReflectionUtils.doWithFields(T::class.java) {
        it.isAccessible = true
        it.set(this, parameters[it.name])
    }
    return this
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

fun <T> String.parse(t: Class<*>): T? {
    if (this.isBlank() || this == ValueConstants.DEFAULT_NONE) return null

    return try {
        when (t) {
            Int::class.java, java.lang.Integer::class.java -> this.toInt()
            Float::class.java, java.lang.Float::class.java -> this.toFloat()
            Double::class.java, java.lang.Double::class.java -> this.toDouble()
            Long::class.java, java.lang.Long::class.java -> this.toLong()
            Boolean::class.java, java.lang.Boolean::class.java -> this.toBoolean()
            BigDecimal::class.java -> this.toBigDecimal()
            BigInteger::class.java -> this.toBigInteger()
            else -> this
        } as T
    } catch (e: NumberFormatException) {
        throw RuntimeException("NumberFormatException, " + e.message)
    }
}

data class Param(val name: String, val type: Class<*>, var value: Any?, var methodParam: Parameter) {
    var fromRequestBody: Boolean = methodParam.isAnnotationPresent(RequestBody::class.java)
    var fromRequestHead: Boolean = methodParam.isAnnotationPresent(RequestHeader::class.java)
    var fromSession: Boolean = methodParam.isAnnotationPresent(SessionAttribute::class.java) || methodParam.isAnnotationPresent(SessionAttributes::class.java)
    var fromCookie: Boolean = methodParam.isAnnotationPresent(CookieValue::class.java)
    //TODO only one of above is allowed
    var isSimpleClass = when (type) {
        java.lang.Integer::class.java, java.lang.Float::class.java,
        java.lang.Double::class.java, java.lang.Long::class.java,
        java.lang.Boolean::class.java, java.lang.String::class.java,
        BigDecimal::class.java, BigInteger::class.java,
        Int::class.java, Float::class.java,
        Double::class.java, Long::class.java,
        Boolean::class.java, String::class.java,
        Char::class.java -> true
        else -> false
    }
}


/**
 * invoke method
 * if its suspend, add continuation
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

