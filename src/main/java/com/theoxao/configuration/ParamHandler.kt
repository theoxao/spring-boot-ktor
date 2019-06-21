@file:Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")

package com.theoxao.configuration

import com.theoxao.resolver.method.*
import com.theoxao.resolver.method.`named-value`.CookieValueMethodArgumentResolver
import com.theoxao.resolver.method.`named-value`.PathVariableMethodArgumentResolver
import com.theoxao.resolver.method.`named-value`.RequestHeaderMethodArgumentResolver
import com.theoxao.resolver.method.`named-value`.RequestParamMethodArgumentResolver
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.pipeline.PipelineContext
import org.springframework.core.MethodParameter
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.ui.Model
import org.springframework.validation.support.BindingAwareConcurrentModel
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.coroutines.Continuation

/**
 * @author theo
 * @date 2019/4/26
 */

data class Param(val type: Class<*>, var value: Any?, var methodParam: Parameter, val method: Method) {
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

val argumentResolvers = listOf<HandlerMethodArgumentResolver>(
        CallArgumentResolver(),
        RequestParamMapMethodArgumentResolver(),
        RequestBodyArgumentResolver(false),
        CookieValueMethodArgumentResolver(),
        PathVariableMethodArgumentResolver(),
        RequestHeaderMethodArgumentResolver(false),
        RequestParamMethodArgumentResolver(),
        RequestParamMapMethodArgumentResolver(),
        RequestHeaderMethodArgumentResolver(true), //ignore annotation
        FinalModelArgumentResolver()
)

@KtorExperimentalLocationsAPI
suspend fun PipelineContext<Unit, ApplicationCall>.handlerParam(method: Method, parameterNameDiscoverer: ParameterNameDiscoverer): Result {
    val methodParams =
            method.parameters.mapIndexed { _, it ->
                Param(it.type, null, it, method)
            }
    val model by lazy { BindingAwareConcurrentModel() }
    methodParams.forEachIndexed { index, param ->
        param.value = when (param.methodParam.type) {
            Model::class.java -> model
            Continuation::class.java -> null
            else -> {
                val methodParameter = MethodParameter(param.method, index)
                methodParameter.initParameterNameDiscovery(parameterNameDiscoverer)
                var result: Any? = null
                for (resolver in argumentResolvers) value@ {
                    if (resolver.supportsParameter(methodParameter)) {
                        try {
                            result = resolver.resolverArgument(methodParameter, null, call.request, null)
                            result ?: continue
                            break
                        } catch (ignore: Exception) {
                            continue
                        }
                    }
                }
                result
            }
        }
    }
    return Result(model, methodParams)
}

data class Result(val model: BindingAwareConcurrentModel, val params: List<Param>)
