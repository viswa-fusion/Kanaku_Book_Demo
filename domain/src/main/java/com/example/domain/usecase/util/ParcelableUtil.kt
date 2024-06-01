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
) : Parcelable {
    var bitmap: Bitmap? = null

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(userId)
        parcel.writeString(name)
        parcel.writeLong(phone)
        parcel.writeString(profilePhotoFilePath)
        parcel.writeParcelable(bitmap, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserProfileSummary> {
        override fun createFromParcel(parcel: Parcel): UserProfileSummary {
            return UserProfileSummary(parcel.readLong(),parcel.readString()?:"",parcel.readLong(),parcel.readString()?:"")
        }

        override fun newArray(size: Int): Array<UserProfileSummary?> {
            return arrayOfNulls(size)
        }
    }
}
