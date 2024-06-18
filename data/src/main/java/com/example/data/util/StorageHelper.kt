package com.example.data.util


import java.io.File

interface StorageHelper {

    companion object{

    val USER_PROFILE_PHOTO_DIRECTORY = "userProfilePhoto"
    val GROUP_PROFILE_PHOTO_DIRECTORY = "GroupProfilePhoto"
    val IMAGE_TYPE_JPG = ".JPG"
    }
    fun getInternalStoragePath(directory: String): File

}

