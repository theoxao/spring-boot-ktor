package com.theoxao.common


/**
 * @author theo
 * @date 2019/4/19
 */
class RestResponse<T>(val code: Int = 200, var data: T?, var msg: String?, private var timestamp: Long = System.currentTimeMillis()) {

    constructor(code: Int) : this(code, null, null)

    companion object {
        fun <T> ok(): RestResponse<T> {
            return RestResponse(200)
        }

        fun error(code: Int = 500): RestResponse<Any> {
            return RestResponse(code)
        }

        fun <T> error(msg: String): RestResponse<T> {
            val record = RestResponse<T>(500)
            record.msg = msg
            return record
        }

        fun <T> notfound(): RestResponse<T> {
            return RestResponse(404)
        }
    }
}

fun <T> RestResponse<T>.withData(data: T?): RestResponse<T> {
    this.data = data
    return this
}

fun <T> RestResponse<T>.withMsg(msg: String?): RestResponse<T> {
    this.msg = msg
    return this
}
