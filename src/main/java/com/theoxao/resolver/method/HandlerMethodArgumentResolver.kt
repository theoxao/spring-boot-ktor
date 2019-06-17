package com.theoxao.resolver.method

import io.ktor.request.ApplicationRequest
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.method.support.ModelAndViewContainer


/**
 * @author theo
 * @date 2019/5/16
 */
interface HandlerMethodArgumentResolver {

    fun supportsParameter(parameter: MethodParameter): Boolean;

    suspend fun resolverArgument(
            parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
            request: ApplicationRequest, binderFactory: WebDataBinderFactory?
    ): Any?
}