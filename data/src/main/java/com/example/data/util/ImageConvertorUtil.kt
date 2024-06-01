package com.example.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory

internal object ImageConvertorUtil {
    fun decodeBitmapFromFile(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}