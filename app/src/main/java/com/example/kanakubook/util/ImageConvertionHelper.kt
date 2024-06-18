package com.example.kanakubook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import android.media.ExifInterface

internal object ImageConversionHelper {

    suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var bitmap: Bitmap? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.let {
                    val exifInterface = ExifInterface(inputStream!!)
                    val orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )

                    inputStream!!.close()
                    inputStream = context.contentResolver.openInputStream(uri)


                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(inputStream, null, options)


                    inputStream!!.close()
                    inputStream = context.contentResolver.openInputStream(uri)

                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }


                    options.inJustDecodeBounds = false
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options)?.let {
                        Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream?.close()
            }
            return@withContext bitmap
        }
}
