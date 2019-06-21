package com.theoxao.resolver.method.`named-value`

import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.RequestHeader


/**
 * @author theo
 * @date 2019/6/6
 */
class RequestHeaderMethodArgumentResolver(private val annotationNotRequired: Boolean) : AbstractNamedValueMethodArgumentResolver() {
    override suspend fun resolveName(name: String, parameter: MethodParameter, request: ApplicationRequest): Any? {
        return request.header(name)
    }

    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val annotation = parameter.getParameterAnnotation(RequestHeader::class.java)
                ?: return RequestHeaderNamedValueInfo(parameter.parameterName!!, false, null)
        return RequestHeaderNamedValueInfo(annotation.name, annotation.required, annotation.defaultValue)
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(RequestHeader::class.java) && !Map::class.java.isAssignableFrom(parameter.nestedIfOptional().nestedParameterType) || annotationNotRequired
    }

    private class RequestHeaderNamedValueInfo(name: String, required: Boolean, defaultValue: String?) : NamedValueInfo(name, required, defaultValue)
}
