package com.theoxao.resolver.method

import io.ktor.request.ApplicationRequest
import io.ktor.response.ApplicationResponse
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.method.support.ModelAndViewContainer


/**
 * @author theo
 * @date 2019/6/17
 */
class CallArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return ApplicationRequest::class.java.isAssignableFrom(parameter.parameterType) || ApplicationResponse::class.java.isAssignableFrom(parameter.parameterType)
    }

    override suspend fun resolverArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
                                          request: ApplicationRequest, binderFactory: WebDataBinderFactory?): Any? {
        return when (parameter.parameterType) {
            is ApplicationRequest -> request
            is ApplicationResponse -> request.call.response
            else -> throw RuntimeException(" parameter type does not supported")
        }
    }
}