package com.theoxao.resolver.method.`named-value`

import io.ktor.request.ApplicationRequest
import org.springframework.core.MethodParameter
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.ModelAndViewContainer


/**
 * @author theo
 * @date 2019/6/6
 */
class PathVariableMethodArgumentResolver : AbstractNamedValueMethodArgumentResolver() {
    override suspend fun resolveName(name: String, parameter: MethodParameter, request: ApplicationRequest): Any? {
        return request.queryParameters[name]
    }

    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val annotation = parameter.getParameterAnnotation(PathVariable::class.java)
        return PathVariableNamedValueInfo(annotation!!)
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        if (!parameter.hasParameterAnnotation(PathVariable::class.java)) {
            return false
        }
        if (Map::class.java.isAssignableFrom(parameter.nestedIfOptional().nestedParameterType)) {
            val paramName = parameter.getParameterAnnotation(PathVariable::class.java)!!.value
            return StringUtils.hasText(paramName)
        }
        return true
    }

    override fun handleResolvedValue(arg: Any, name: String, parameter: MethodParameter, mavContainer: ModelAndViewContainer, request: ApplicationRequest) {
        super.handleResolvedValue(arg, name, parameter, mavContainer, request)
    }

    private class PathVariableNamedValueInfo(annotation: PathVariable) : NamedValueInfo(annotation.name, annotation.required, ValueConstants.DEFAULT_NONE)
}