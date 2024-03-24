package com.technicaltest.core.service.util

import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException

object NetworkUtils {

    fun Exception.handleException(): String {
        return when (this) {
            is UnknownHostException,
            is SocketException -> "Pastikan Anda Memiliki Koneksi Internet dan Coba Lagi"

            is SocketTimeoutException -> "Koneksi Timeout. Coba Lagi"
            is SSLPeerUnverifiedException -> "Sertifikat Keamanan tidak valid. " +
                    "Anda tidak diperkenankan masuk ke dalam aplikasi."

            else -> this.message.toString()
        }
    }

    private fun getErrorMessage(code: Int): String {
        return when (code) {
            401 -> "Unauthorised"
            404 -> "Not found"
            403 -> "Forbidden"
            500 -> "Internal Server Error"
            else -> "Something went wrong, please try again later. (response code: ${code})"
        }
    }

    fun Exception.codeException(): Int {
        return when (this) {
            is SSLPeerUnverifiedException -> 603
            else -> 500
        }
    }
}