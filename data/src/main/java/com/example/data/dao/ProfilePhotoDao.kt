package com.example.kanakunote.data_layer.dao

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.room.Dao
import com.example.domain.repository.response.DataLayerErrorCode
import com.example.domain.repository.response.DataLayerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
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
            val stream: OutputStream = FileOutputStream(fileLocation)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            DataLayerResponse.Success(true)
        } catch (e: IOException) {
            e.printStackTrace()
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }


suspend fun getProfilePhoto(filePath: String): DataLayerResponse<Bitmap> = withContext(Dispatchers.IO) {

    return@withContext try {
            DataLayerResponse.Success(BitmapFactory.decodeFile(filePath))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        DataLayerResponse.Error(DataLayerErrorCode.FILE_NOT_FOUND)
    } catch (e: Exception) {
        e.printStackTrace()
        DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
    }
}


//    fun getImageUri(context: Context, file: File): Uri? {
//        return try {
//            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
//        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
//            null
//        }
//    }
}