package com.example.domain.model

import android.graphics.Bitmap

data class UserEntryData(
    val name: String,
    val phone: Long,
    val password: String,
    val dateOfBirth: Long?
)

data class UserProfileData(
    val userId: Long,
    val name: String,
    val phone: Long,
    val amountToGet: Double,
    val amountToGive: Double
)

data class UserProfileSummary(
    val userId: Long,
    val name: String,
    val phone: Long
) {
    var profile: Bitmap? = null
    var connectionId: Long? = null
    var pay: Double = 0.0
    var get: Double = 0.0
}