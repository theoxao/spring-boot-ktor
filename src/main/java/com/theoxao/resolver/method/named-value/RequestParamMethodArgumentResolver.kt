package com.theoxao.resolver.method.`named-value`

import com.theoxao.configuration.KtorMultipartFile
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.request.ApplicationRequest
import io.ktor.request.receiveMultipart
import org.springframework.core.MethodParameter
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ValueConstants


/**
 * @author theo
 * @date 2019/6/6
 */
class RequestParamMethodArgumentResolver : AbstractNamedValueMethodArgumentResolver() {

    override suspend fun resolveName(name: String, parameter: MethodParameter, request: ApplicationRequest): Any? {
        var arg: Any? = null

        if (KtorMultipartFile::class.java.isAssignableFrom(parameter.nestedParameterType)) {
            val files: MutableList<KtorMultipartFile> = mutableListOf()
            val multiPartData = request.call.receiveMultipart()
            multiPartData.forEachPart {
                if (it is PartData.FileItem && it.name == name) {
                    files.add(KtorMultipartFile(it))
                    it.dispose()
                }
            }
            return if (files.size == 1) files[0] else files
        }
        var paramValues: List<String>? = null
        if (arg == null) {
            request.queryParameters.forEach { p, list ->
                if (p == name) {
                    paramValues = list
                }
            }
            if (paramValues != null) {
                arg = if (paramValues!!.size == 1) paramValues!![0] else paramValues
            }
        }
        return arg
    }

    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val ann = parameter.getParameterAnnotation(RequestParam::class.java)
        return ann?.let { RequestParamNamedValueInfo(it) } ?: RequestParamNamedValueInfo()
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return if (parameter.hasParameterAnnotation(RequestParam::class.java)) {
            if (Map::class.java.isAssignableFrom(parameter.nestedIfOptional().nestedParameterType)) {
                val paramName = parameter.getParameterAnnotation(RequestParam::class.java)!!.name
                StringUtils.hasText(paramName)
            } else {
                true
            }
        } else {
            if (parameter.hasParameterAnnotation(RequestPart::class.java)) {
                false
            } else
                KtorMultipartFile::class.java.isAssignableFrom(parameter.nestedIfOptional().nestedParameterType)
        }
    }

    private class RequestParamNamedValueInfo : NamedValueInfo {
        constructor() : super("", false, ValueConstants.DEFAULT_NONE)
        constructor(annotation: RequestParam) : super(annotation.name, annotation.required, annotation.defaultValue)
    }
}