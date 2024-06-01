package com.example.kanakubook

import android.content.Context
import com.example.data.util.StorageHelper
import java.io.File

class StorageHelperImpl(private val context: Context) : StorageHelper {
    override fun getInternalStoragePath(directory: String): File {
        return File(context.filesDir,directory)
    }
}