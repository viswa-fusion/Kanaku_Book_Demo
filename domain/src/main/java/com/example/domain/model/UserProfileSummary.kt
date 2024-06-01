package com.example.domain.model

import android.graphics.Bitmap


data class UserProfileSummary(
    val userId: Long,
    val name:String,
    val phone: Long,
    val profilePhotoFilePath: String,

    ){
    var bitmap: Bitmap? = null
}
