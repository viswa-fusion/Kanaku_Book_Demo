package com.example.kanakunote.data_layer.dao

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.room.Dao
import com.example.domain.repository.response.DataLayerErrorCode
import com.example.domain.repository.response.DataLayerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.*
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


@Dao
interface ProfilePhotoDao {
    suspend fun saveImage(
        bitmap: Bitmap?,
        fileLocation: File
    ): DataLayerResponse<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val stream: OutputStream = BufferedOutputStream(FileOutputStream(fileLocation))
            val resizedBitmap = bitmap?.let { resizeBitmapIfNeeded(it) }
            resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 60, stream)
            stream.flush()
            stream.close()
            DataLayerResponse.Success(true)
        } catch (e: IOException) {
            e.printStackTrace()
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxSize = 2048
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth = if (aspectRatio > 1) maxSize else (maxSize * aspectRatio).toInt()
        val newHeight = if (aspectRatio < 1) maxSize else (maxSize / aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }



    suspend fun getProfilePhoto(filePath: String): DataLayerResponse<Bitmap> =
        withContext(Dispatchers.IO) {


            return@withContext try {
                val bitmap = BitmapFactory.decodeFile(filePath)
                DataLayerResponse.Success(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                DataLayerResponse.Error(DataLayerErrorCode.FILE_NOT_FOUND)
            } catch (e: Exception) {
                e.printStackTrace()
                DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
            }
        }




    fun rotateBitmap(bitmap: Bitmap?, orientation: Int): Bitmap? {
        if (bitmap == null) return null
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return try {
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            rotatedBitmap
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            bitmap
        }
    }
}