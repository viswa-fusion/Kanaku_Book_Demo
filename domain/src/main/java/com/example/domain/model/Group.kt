package com.example.domain.model

import android.graphics.Bitmap

data class Group(
    val id: Long,
    val name: String,
    val profilePicture: String,
    val createdBy: Long,
    val members: List<UserProfileSummary>
){
    var bitmap: Bitmap? = null
}