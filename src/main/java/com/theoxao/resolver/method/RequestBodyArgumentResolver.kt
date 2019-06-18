package com.theoxao.resolver.method

import io.ktor.request.ApplicationRequest
import io.ktor.request.receiveOrNull
import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.method.support.ModelAndViewContainer
import java.math.BigDecimal
import java.math.BigInteger

class RequestBodyArgumentResolver(private val annotationNotRequired: Boolean) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(RequestBody::class.java) || annotationNotRequired
    }

    override suspend fun resolverArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?, request: ApplicationRequest, binderFactory: WebDataBinderFactory?): Any? {
        return request.call.receiveOrNull(parameter.parameterType.kotlin)
    }
}
