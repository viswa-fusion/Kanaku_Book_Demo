package com.example.domain.Converters

import androidx.room.TypeConverter
enum class PaidStatus  {
    Paid,UnPaid
}

class Converters {
    @TypeConverter
    fun fromPaidStatus(value: PaidStatus): String {
        return value.name
    }

    @TypeConverter
    fun toPaidStatus(value: String): PaidStatus {
        return enumValueOf(value)
    }
}