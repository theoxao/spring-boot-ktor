package com.theoxao.resolver

import io.ktor.application.ApplicationCall


/**
 * @author theo
 * @date 2019/5/13
 */
interface Filter {

    fun before(call: ApplicationCall)

    fun after(call: ApplicationCall)

}