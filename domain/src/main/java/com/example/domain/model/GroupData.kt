package com.example.domain.model

import android.graphics.Bitmap

data class GroupData(
    val id: Long,
    val name: String,
    val createdBy: Long,
    val lastActiveTime: Long,
    val members: List<UserProfileSummary>
){
    var profile: Bitmap? = null
}

data class GroupEntry(
    val name: String,
    val profilePicture: Bitmap?,
    val createdBy: Long,
    val lastActiveTime: Long,
    val members: List<Long>
)

data class GroupSummery(
    val name: String,
    val profilePicture: String,
    val createdBy: Long
)
