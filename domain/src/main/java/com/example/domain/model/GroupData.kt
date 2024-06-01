package com.example.domain.model

import android.graphics.Bitmap

data class GroupData(
    val name: String,
    val profilePicture: Bitmap?,
    val createdBy: Long,
    val members: List<Long>
)
