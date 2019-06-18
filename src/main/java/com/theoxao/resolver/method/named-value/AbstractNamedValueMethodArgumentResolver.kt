package com.theoxao.resolver.method.`named-value`

import com.theoxao.resolver.method.HandlerMethodArgumentResolver
import io.ktor.request.ApplicationRequest
import org.springframework.beans.factory.config.BeanExpressionContext
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.RequestScope
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.concurrent.ConcurrentHashMap


/**
 * @author theo
 * @date 2019/5/16
 */
abstract class AbstractNamedValueMethodArgumentResolver : HandlerMethodArgumentResolver {

    private val configurableBeanFactory: ConfigurableBeanFactory?

    private val expressionContext: BeanExpressionContext?

    private val namedValueInfoCache = ConcurrentHashMap<MethodParameter, NamedValueInfo>(256)

    constructor() {
        this.configurableBeanFactory = null
        this.expressionContext = null
    }

    constructor(beanFactory: ConfigurableBeanFactory?) {
        this.expressionContext = beanFactory?.let { BeanExpressionContext(beanFactory, RequestScope()) }
        this.configurableBeanFactory = beanFactory
    }


    override suspend fun resolverArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
                                          request: ApplicationRequest, binderFactory: WebDataBinderFactory?
    ): Any? {
        val namedValueInfo = getNamedValueInfo(parameter)
        val resolvedName = resolveStringValue(namedValueInfo.name)

        resolvedName
                ?: throw java.lang.IllegalArgumentException("Specified name must not resolve to null: [${namedValueInfo.name}]")
        var arg = resolveName(resolvedName.toString(), parameter, request)
        if (arg == null) {
            if (namedValueInfo.defaultValue != null) {
                arg = resolveStringValue(namedValueInfo.defaultValue)
            } else if (namedValueInfo.required) {
                throw java.lang.IllegalArgumentException("Missing argument '${namedValueInfo.name} ' " +
                        "for method parameter of type ${parameter.nestedParameterType.simpleName}")
            }
            arg = handleNullValue(namedValueInfo.name, arg, parameter.parameterType)
        } else if ("" == arg && namedValueInfo.defaultValue != null) {
            arg = resolveStringValue(namedValueInfo.defaultValue)
        }
        //TODO use data binder

        return arg
    }

    private fun handleNullValue(name: String, value: Any?, paramType: Class<*>): Any? {
        return value
                ?: if (paramType == Boolean::class.java || paramType == Boolean::class) false
                else if (!paramType.isPrimitive) value
                else throw IllegalArgumentException("Optional ${paramType.simpleName} parameter ' $name " +
                        "' is present but cannot be translated into a null value due to being declared as a " +
                        "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.")
    }

    abstract suspend fun resolveName(name: String, parameter: MethodParameter, request: ApplicationRequest): Any?

    private fun resolveStringValue(value: String): Any? {
        this.configurableBeanFactory ?: return value
        val embeddedValue = this.configurableBeanFactory.resolveEmbeddedValue(value)
        val expressionResolver = this.configurableBeanFactory.beanExpressionResolver
        expressionResolver ?: return value
        return expressionResolver.evaluate(embeddedValue, this.expressionContext!!)
    }

    private fun getNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        var namedValueInfo = this.namedValueInfoCache[parameter]
        if (namedValueInfo == null) {
            namedValueInfo = createNamedValueInfo(parameter)
            namedValueInfo = updateNamedValueInfo(parameter, namedValueInfo)
        }
        return namedValueInfo
    }

    private fun updateNamedValueInfo(parameter: MethodParameter, info: NamedValueInfo): NamedValueInfo {
        var name = info.name
        if (info.name.isBlank()) {
            name = parameter.parameterName
                    ?: throw IllegalArgumentException("Name for argument type [ ${parameter.nestedParameterType.name} ] " +
                            "not available, and parameter name information not found in class file either.")
        }
        val defaultValue = if (ValueConstants.DEFAULT_NONE == info.defaultValue) null else info.defaultValue
        return NamedValueInfo(name, info.required, defaultValue)
    }


    abstract fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo

    open fun handleResolvedValue(arg: Any, name: String, parameter: MethodParameter,
                                 mavContainer: ModelAndViewContainer, request: ApplicationRequest) {
    }

}


open class NamedValueInfo(val name: String, val required: Boolean = false, val defaultValue: String?)