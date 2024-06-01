package com.example.kanakubook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

internal object ImageConversionHelper {

    suspend fun loadBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
        return withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var bitmap: Bitmap? = null
            if (uri == null) return@withContext null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.let {
                    bitmap = BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream?.close()
            }
            return@withContext bitmap
        }
    }
}



