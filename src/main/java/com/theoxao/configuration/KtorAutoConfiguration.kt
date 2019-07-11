package com.theoxao.configuration

import com.fasterxml.jackson.core.JsonParser
import com.theoxao.Application
import com.theoxao.filter.Filter
import com.theoxao.util.GsonUtil
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.PartialContent
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EngineAPI
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.Sessions
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.LocalVariableTableParameterNameDiscoverer
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMethod.*
import java.lang.reflect.Method
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

    @Resource
    private lateinit var context: ApplicationContext

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
                    this.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
                }
            }
            install(Locations)
            install(Sessions)
            install(PartialContent)
            install(CORS){
                anyHost()
            }
            if (Class.forName("io.ktor.freemarker.FreeMarker") != null)
                install(FreeMarker) {
                    templateLoader = ClassTemplateLoader(Application::class.java, "/${properties.templatesRoot}")
                }

            val beans = context.getBeansWithAnnotation(Controller::class.java).values

            val allDefinitions = beans.flatMap { bean ->
                val classMapping = bean.javaClass.getDeclaredAnnotation(RequestMapping::class.java)
                bean.javaClass.methods.filter {
                    it.getMappingAnnotation() != null
                }.map {
                    val mappingAnnotation = it.getMappingAnnotation()!!
                    val list = mappingAnnotation.value.flatMap { child ->
                        classMapping.value.map { parent ->
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
                        call.respond("Unknown Exception")
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

    private fun Method.getMappingAnnotation(): MappingAnnotation? {
        return this.getDeclaredAnnotation(RequestMapping::class.java)?.let {
            MappingAnnotation(it.value, it.method)
        } ?: this.getDeclaredAnnotation(GetMapping::class.java)?.let {
            MappingAnnotation(it.value, arrayOf(GET))
        } ?: this.getDeclaredAnnotation(PutMapping::class.java)?.let {
            MappingAnnotation(it.value, arrayOf(PUT))
        } ?: this.getDeclaredAnnotation(PostMapping::class.java)?.let {
            MappingAnnotation(it.value, arrayOf(POST))
        } ?: this.getDeclaredAnnotation(DeleteMapping::class.java)?.let {
            MappingAnnotation(it.value, arrayOf(DELETE))
        }
    }

    data class MappingAnnotation(val value: Array<String>, val method: Array<RequestMethod>)

    @KtorExperimentalLocationsAPI
    private fun Route.mapping(definition: RouteDefinition) {
        val parameterNameDiscoverer = LocalVariableTableParameterNameDiscoverer()
        val beanMaps = context.getBeansOfType(Filter::class.java)
        val method = definition.method
        val bean = definition.bean
        if (definition.methods.isEmpty()) {
            definition.methods = values()
        }
        definition.methods.forEach { requestMethod ->
            definition.uri.forEach { uri ->
                route(uri, HttpMethod.parse(requestMethod.name)) {
                    handle {
                        beanMaps.values.forEach {
                            it.before(call)
                        }
                        val (model, methodParams) = handlerParam(method, parameterNameDiscoverer)
                        val message = method.invokeSuspend(bean, methodParams.map { it.value }.toTypedArray())
                        handleView(message, definition, model)
                        beanMaps.values.forEach {
                            it.after(call)
                        }
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
            val resultStr = result as String
            when {
                resultStr.startsWith("redirect:") -> call.respondRedirect(resultStr.removePrefix("redirect:"), true)
                resultStr.startsWith("static:") -> call.respondRedirect("/${properties.staticRoot}/${resultStr.removePrefix("static:")}")
                //TODO what was i thinking
                else -> call.respond(FreeMarkerContent(result, GsonUtil.toMap(GsonUtil.toJson(model!!.asMap()))))
            }
        }
    }
}

data class RouteDefinition(val method: Method, val bean: Any, var methods: Array<RequestMethod>, val uri: List<String>)

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

