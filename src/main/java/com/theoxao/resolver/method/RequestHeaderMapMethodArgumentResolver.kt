package com.theoxao.resolver.method

import io.ktor.request.ApplicationRequest
import io.ktor.util.toMap
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.*


/**
 * @author theo
 * @date 2019/6/17
 */
class RequestHeaderMapMethodArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(RequestHeader::class.java) && Map::class.java.isAssignableFrom(parameter.parameterType)
    }

    override suspend fun resolverArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
                                          request: ApplicationRequest, binderFactory: WebDataBinderFactory?): Any? {
        val paramType = parameter.parameterType
        if (MultiValueMap::class.java.isAssignableFrom(paramType)) {
            val result: MultiValueMap<String, String> =
                    if (HttpHeaders::class.java.isAssignableFrom(paramType)) HttpHeaders() else LinkedMultiValueMap()
            request.headers.toMap().forEach { (k, values) ->
                values.forEach {
                    result.add(k, it)
                }
            }
            return result
        }
        val result = LinkedHashMap<String, String>()
        request.headers.toMap().forEach { (k, values) ->
            result[k] = values[0]
        }
        return result
    }
}