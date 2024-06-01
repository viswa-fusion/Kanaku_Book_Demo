package com.example.domain.model

data class UserProfileData(
    val userId:Long,
    val name: String,
    val phone: Long,
    val profilePicturePath: String,
    val amountToGet: Double,
    val amountToGive: Double
)
