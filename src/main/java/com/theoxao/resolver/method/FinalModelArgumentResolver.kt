package com.theoxao.resolver.method

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.locations
import io.ktor.request.ApplicationRequest
import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.method.support.ModelAndViewContainer
import java.math.BigDecimal
import java.math.BigInteger


/**
 * @author theo
 * @date 2019/6/18
 */
class FinalModelArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = true

    @KtorExperimentalLocationsAPI
    override suspend fun resolverArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?, request: ApplicationRequest, binderFactory: WebDataBinderFactory?): Any? {
        return if (isSimple(parameter.parameterType))
            request.call.parameters[parameter.parameterName!!]?.parse(parameter.parameterType)
        else
            request.call.locations.resolve(parameter.parameterType.kotlin, request.call)
    }
}


fun isSimple(type: Class<*>) = when (type) {
    java.lang.Integer::class.java, java.lang.Float::class.java,
    java.lang.Double::class.java, java.lang.Long::class.java,
    java.lang.Boolean::class.java, java.lang.String::class.java,
    BigDecimal::class.java, BigInteger::class.java,
    Int::class.java, Float::class.java,
    Double::class.java, Long::class.java,
    Boolean::class.java, String::class.java,
    Char::class.java -> true
    else -> false
}

fun <T> String.parse(t: Class<T>): T? {
    if (this.isBlank() || this == ValueConstants.DEFAULT_NONE) return null
    return try {
        when (t) {
            Int::class.java, java.lang.Integer::class.java -> this.toInt()
            Float::class.java, java.lang.Float::class.java -> this.toFloat()
            Double::class.java, java.lang.Double::class.java -> this.toDouble()
            Long::class.java, java.lang.Long::class.java -> this.toLong()
            Boolean::class.java, java.lang.Boolean::class.java -> this.toBoolean()
            BigDecimal::class.java -> this.toBigDecimal()
            BigInteger::class.java -> this.toBigInteger()
            else -> this
        } as T
    } catch (e: NumberFormatException) {
        throw RuntimeException("NumberFormatException, " + e.message)
    }
}
