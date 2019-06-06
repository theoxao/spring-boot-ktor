package com.theoxao.resolver.method

import io.ktor.request.ApplicationRequest
import io.ktor.util.AttributeKey
import org.springframework.core.MethodParameter
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.method.support.ModelAndViewContainer


/**
 * @author theo
 * @date 2019/5/16
 */

class PathVariableMapMethodArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val ann = parameter.getParameterAnnotation(PathVariable::class.java)
        return (ann != null && Map::class.java.isAssignableFrom(parameter.parameterType)
                && !StringUtils.hasText(ann.value))
    }

    override suspend fun resolverArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer,
                                          request: ApplicationRequest, binderFactory: WebDataBinderFactory): Any? {
        //TODO
        return null
    }


}