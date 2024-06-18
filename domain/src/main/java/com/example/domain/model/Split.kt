package com.example.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.example.domain.Converters.PaidStatus

data class SplitEntry(
    val splitUserId: Long,
    val splitAmount: Double,
    val paidStatus: PaidStatus,
    val paidTime: Long? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readDouble(),
        enumValueOf(parcel.readString()!!),
        parcel.readValue(Long::class.java.classLoader) as Long?
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(splitUserId)
        parcel.writeDouble(splitAmount)
        parcel.writeString(paidStatus.name)
        parcel.writeValue(paidTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SplitEntry> {
        override fun createFromParcel(parcel: Parcel): SplitEntry {
            return SplitEntry(parcel)
        }

        override fun newArray(size: Int): Array<SplitEntry?> {
            return arrayOfNulls(size)
        }
    }
}