package com.theoxao.resolver.method

import io.ktor.request.ApplicationRequest
import io.ktor.util.toMap
import org.springframework.core.MethodParameter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.method.support.ModelAndViewContainer


/**
 * @author theo
 * @date 2019/6/13
 */
class RequestParamMapMethodArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val requestParam = parameter.getParameterAnnotation(RequestParam::class.java)
        if (requestParam != null && Map::class.java.isAssignableFrom(parameter.parameterType))
            return !StringUtils.hasText(requestParam.name)
        return false
    }

    override suspend fun resolverArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
                                          request: ApplicationRequest, binderFactory: WebDataBinderFactory?): Any? {
        val paramType = parameter.parameterType
        val parameters = request.queryParameters.toMap()
        return if (MultiValueMap::class.java.isAssignableFrom(paramType)) {
            val result = LinkedMultiValueMap<String, String>()
            parameters.forEach { (k, v) ->
                v.forEach {
                    result.add(k, it)
                }
            }
            result
        } else {
            val result = LinkedHashMap<String, String>(parameters.size)
            parameters.forEach { (k, v) ->
                result[k] = v[0]
            }
            result
        }
    }
}