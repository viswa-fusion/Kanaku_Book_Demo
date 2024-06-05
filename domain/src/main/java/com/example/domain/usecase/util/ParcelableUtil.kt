package com.example.domain.usecase.util

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.example.domain.model.UserProfileSummary


data class UserProfileSummaryParcel(
    val userId: Long,
    val name: String,
    val phone: Long,
    val profilePhotoFilePath: String,
)  {
    var bitmap: Bitmap? = null
}
