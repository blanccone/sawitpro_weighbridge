package com.blanccone.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Base64
import androidx.core.content.ContextCompat
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    @JvmStatic
    fun isConnected(context: Context): Boolean {
        val result: Boolean
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        return result
    }

    fun getCameraPermissions() = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun isAllowModifiedStorageAndCamera(context: Context): Boolean {
        when{
            !Manifest.permission.CAMERA.isGranted(context) -> return false
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !Manifest.permission.READ_MEDIA_IMAGES.isGranted(context) -> return false
            !Manifest.permission.READ_EXTERNAL_STORAGE.isGranted(context) -> return false
        }
        return true
    }

    private fun String.isGranted(context: Context):Boolean{
        return ContextCompat.checkSelfPermission(context, this) == PackageManager.PERMISSION_GRANTED
    }

    fun decodeBase64(encoded: String): String {
        val dataDec = Base64.decode(encoded, Base64.DEFAULT)
        var decodedString = ""
        try {
            decodedString = String(dataDec, StandardCharsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } finally {
            return decodedString
        }
    }

    fun generateUniqueId(inputString: String): Long {
        // Menggunakan SHA-256 sebagai contoh, bisa diganti dengan algoritma lain jika diinginkan
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(inputString.toByteArray())

        // Mengambil 8 byte pertama dari hasil hash sebagai long
        val uniqueId = java.nio.ByteBuffer.wrap(hashedBytes, 0, 8).long

        return uniqueId
    }

    fun Date.toFormatString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
}