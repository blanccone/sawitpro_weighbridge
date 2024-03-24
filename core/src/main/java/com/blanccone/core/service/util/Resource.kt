package com.technicaltest.core.service.util

sealed class Resource<T>(
    val code: Int = 200,
    val message: String? = null,
    val data: T? = null
) {
    class Loading<T> : Resource<T>(code = 0)
    class Success<T>(data: T?) : Resource<T>(data = data)
    class Error<T>(
        message: String?,
        data: T? = null,
        code: Int = 500,
    ) : Resource<T>(code, message, data)
}