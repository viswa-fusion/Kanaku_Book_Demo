package com.example.domain.model

import android.graphics.Bitmap

data class UserEntryData(
    val name: String,
    val phone: Long,
    val password: String
)

data class UserProfileData(
    val userId:Long,
    val name: String,
    val phone: Long,
    val profilePicturePath: String,
    val amountToGet: Double,
    val amountToGive: Double
)

data class UserProfileSummary(
    val userId: Long,
    val name:String,
    val phone: Long
){
    var profile: Bitmap? = null
}