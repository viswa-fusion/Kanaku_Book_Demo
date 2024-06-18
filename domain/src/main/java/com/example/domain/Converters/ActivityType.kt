package com.example.domain.Converters

import androidx.room.TypeConverter

enum class ActivityType {
    ADD_FRIEND,
    CREATE_GROUP,
    ADD_EXPENSE,
    ADD_MEMBER_TO_GROUP,
    PAY_FOR_EXPENSE,
    SPLIT_MEMBER_PAY;
    class Converters {
        @TypeConverter
        fun fromActivityType(value: ActivityType): String {
            return value.name
        }

        @TypeConverter
        fun toActivityType(value: String): ActivityType {
            return enumValueOf(value)
        }
    }
}