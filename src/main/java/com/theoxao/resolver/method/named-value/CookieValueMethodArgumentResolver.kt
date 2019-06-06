package com.theoxao.resolver.method.`named-value`

import io.ktor.request.ApplicationRequest
import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.CookieValue


/**
 * @author theo
 * @date 2019/6/6
 */
class CookieValueMethodArgumentResolver : AbstractNamedValueMethodArgumentResolver() {
    override suspend fun resolveName(name: String, parameter: MethodParameter, request: ApplicationRequest): Any? {
        return if (String::class.java.isAssignableFrom(parameter.nestedParameterType)) {
            request.cookies[name]
        } else {
            null
        }
    }

    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val annotation = parameter.getParameterAnnotation(CookieValue::class.java)
        return CookieValueNamedValueInfo(annotation)
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CookieValue::class.java)
    }

    private class CookieValueNamedValueInfo(annotation: CookieValue) : NamedValueInfo(annotation.name, annotation.required, annotation.defaultValue)
}