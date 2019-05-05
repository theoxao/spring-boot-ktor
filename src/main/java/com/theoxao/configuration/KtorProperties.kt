package com.theoxao.configuration

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "spring.ktor")
open class KtorProperties {
    open var host: String = "0.0.0.0"
    open var port: Int = 8088
    open var enableTrace = false
    open var engine = "Netty"
    open var staticRoot = "static"
    open val templatesRoot = "templates"
}
