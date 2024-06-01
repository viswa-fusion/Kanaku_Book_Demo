package com.example.data.util


import java.io.File

interface StorageHelper {

    companion object{

    val USER_PROFILE_PHOTO_DIRECTORY = "userProfilePhoto"
    val GROUP_PROFILE_PHOTO_DIRECTORY = "GroupProfilePhoto"
    val IMAGE_TYPE_JPG = ".JPG"
    }
    fun getInternalStoragePath(directory: String): File

//    fun generateAbsolutePath(fileName: String, directoryType: ImageDirectoryType): File{
//        val pathDir = File(getInternalStoragePath(),getDir(directoryType))
//        val filePath = File(pathDir,"${fileName}$IMAGE_TYPE")
//
//        return filePath
//    }

//    private fun getDir(directoryType: ImageDirectoryType): String{
//        return when(directoryType){
//            ImageDirectoryType.User -> USER_PROFILE_PHOTO_DIRECTORY
//            ImageDirectoryType.Group -> Group_PROFILE_PHOTO_DIRECTORY
//        }
//    }
}

