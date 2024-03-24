package com.blanccone.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import com.blanccone.core.R
import com.blanccone.core.util.Utils.toFormatString
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import kotlin.math.roundToInt

object FileUtils {

    val fileSuffix = (Calendar.getInstance().time).toFormatString("yyMMddHHSSS")

    fun bitmapToFile(bm: Bitmap, dir: String?, name: String?): File? {
        val file = File(dir, name)
        if (file.exists()) file.delete()
        try {
            val fos = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    fun getPictureDir(context: Context): File? {
        val dir = context.getExternalFilesDir(
            ("SawitPro Weighbridge" + File.separator
                    + Environment.DIRECTORY_PICTURES
                    + File.separator)
        )
        if (dir != null && !dir.exists()) {
            dir.mkdir()
        }
        return dir
    }

    fun isFileOverSize(file: File, size: Int = 5): Boolean {
        val fileSizeInKB: Long = file.length() / 1024
        return fileSizeInKB >= (1024 * size)
    }

    fun getFileNameTemplate(fieldName: String, ext: String): String {
        return "SAWITPRO_" +
                fieldName
                    .replace(" ", "_")
                    .replace("/", "")
                    .uppercase() +
                "_${fileSuffix}.$ext"
    }

    fun getFileName(source: String): String {
        val split = source.split("/")
        return split.last()
    }

    fun getFileNameWithoutExt(source: String): String {
        val currentFileName = getFileName(source)
        return currentFileName.split(".")[0]
    }

    fun getPathConverted(
        context: Context,
        path: String,
        fileName: String,
        reqWidth: Int? = 800,
        reqHeight: Int? = 800
    ): String? {
        return compressImage(context, path, fileName, reqWidth, reqHeight)
    }

    fun compressImage(
        context: Context,
        path: String,
        fileName: String,
        reqWidth: Int? = null,
        reqHeight: Int? = null
    ): String? {
        val options = BitmapFactory.Options()

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(path, options)
        val actualWidth = reqWidth ?: options.outWidth
        val actualHeight = reqHeight ?: options.outHeight
        options.inSampleSize = calculateInSampleSize(
            options = options,
            reqWidth = actualWidth,
            reqHeight = actualHeight
        )
        options.inJustDecodeBounds = false

        try {
            options.inPurgeable = true
            options.inInputShareable = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        options.inTempStorage = ByteArray(16 * 1024)
        try {
            // load the bitmap from its path
            bmp = BitmapFactory.decodeFile(path, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        // check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            bmp = Bitmap.createBitmap(
                bmp, 0, 0,
                bmp.width, bmp.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return getFileNameCompressed(context, bmp, fileName)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    private fun getFileNameCompressed(
        context: Context,
        finalData: Bitmap?,
        fileName: String
    ): String? {
        val newImages = "$fileName.jpg"
        val dir = getPictureDir(context)
        val fileImage = File(dir, newImages)
        val imagePath = fileImage.path
        try {
            FileOutputStream(imagePath).use { fOut ->
                val imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1)
                val imageType = imageName.substring(imageName.lastIndexOf(".") + 1)
                try {
                    FileOutputStream(imagePath).use { fos ->
                        when {
                            imageType.equals("png", ignoreCase = true) -> {
                                finalData!!.compress(Bitmap.CompressFormat.PNG, 100, fos)
                            }
                            imageType.equals("jpg", ignoreCase = true) -> {
                                finalData!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            }
                            else -> finalData!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                fOut.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finalData!!.recycle()
        return fileImage.absolutePath
    }
}