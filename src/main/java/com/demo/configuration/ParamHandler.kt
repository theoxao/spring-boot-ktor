@file:Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")

package com.demo.configuration

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.locations
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveOrNull
import io.ktor.response.ApplicationResponse
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import org.springframework.ui.Model
import org.springframework.util.ReflectionUtils
import org.springframework.validation.support.BindingAwareConcurrentModel
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.coroutines.Continuation

/**
 * @author theo
 * @date 2019/4/26
 */

data class Param(val name: String, val type: Class<*>, var value: Any?, var methodParam: Parameter) {
    var fromRequestBody: Boolean = methodParam.isAnnotationPresent(RequestBody::class.java)
    var fromRequestHead: Boolean = methodParam.isAnnotationPresent(RequestHeader::class.java)
    var fromSession: Boolean = methodParam.isAnnotationPresent(SessionAttribute::class.java) || methodParam.isAnnotationPresent(SessionAttributes::class.java)
    var fromCookie: Boolean = methodParam.isAnnotationPresent(CookieValue::class.java)
    var fromMultipart: Boolean = methodParam.type == MultipartFile::class.java
    var isList: Boolean = when (type) {
        List::class.java, java.util.List::class.java,
        ArrayList::class.java, java.util.ArrayList::class.java, java.util.LinkedList::class.java -> true
        else -> false
    }
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


@KtorExperimentalLocationsAPI
suspend fun PipelineContext<Unit, ApplicationCall>.handlerParam(methodParams: List<Param>): Model {
    val model by lazy { BindingAwareConcurrentModel() }
    methodParams.forEachIndexed { index, param ->
        param.value = when (param.methodParam.type) {
            Model::class.java -> model
            ApplicationRequest::class.java -> call.request
            ApplicationResponse::class.java -> call.response
            Continuation::class.java -> null
            else -> when {
                param.fromMultipart -> {
                    val multipart = call.receiveMultipart()
                    var file: MultipartFile? = null
                    multipart.forEachPart {
                        it as PartData.FileItem
                        if (it.name == param.name) {
                            val ext = File(it.originalFileName).extension
                            file = KtorMultipartFile(it)
                        }
                        it.dispose()
                    }
                    file
                }
                param.fromRequestBody -> {
                    call.receiveOrNull(param.type.kotlin)
                }
                param.fromRequestHead -> {
                    val rh = (param.methodParam.getAnnotation(RequestHeader::class.java))
                    (call.request.header(rh.name) ?: call.request.header(param.name)
                    ?: rh.defaultValue).parse(param.type)
                }
                param.fromSession -> {
                    //TODO some problems
                    val sa = (param.methodParam.getAnnotation(SessionAttribute::class.java))
                    val sessions = call.sessions
                    sessions.get(sa.name) ?: sessions.get(param.name)
                }
                param.fromCookie -> {
                    val kv = (param.methodParam.getAnnotation(CookieValue::class.java))
                    (call.request.cookies[kv.name] ?: call.request.cookies[param.name]
                    ?: kv.defaultValue).parse(param.type)
                }
                param.isList -> {
                    //only support url parameter
                    //TODO this isn't right
                    val p = call.parameters[param.name]
                    val generic = param.type.genericInterfaces[0]
                    if (ParameterizedType::class.java.isInstance(generic)) {
                        val pt = generic as ParameterizedType
                        val rawType = pt.rawType as Class<*>
                    }
                    1
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
    return model
}

inline fun <reified T> T.fromParam(parameters: Parameters): T {
    ReflectionUtils.doWithFields(T::class.java) {
        it.isAccessible = true
        it.set(this, parameters[it.name])
    }
    return this
}

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