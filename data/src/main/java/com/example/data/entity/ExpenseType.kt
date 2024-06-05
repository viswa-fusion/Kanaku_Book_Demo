package com.example.data.entity

import androidx.room.TypeConverter
import com.example.domain.Converters.PaidStatus

enum class ExpenseType {
    GroupExpense, FriendsExpense
}

class Converters {
    @TypeConverter
    fun fromExpenseType(value: ExpenseType): String {
        return value.name
    }

    @TypeConverter
    fun toExpenseType(value: String): ExpenseType {
        return enumValueOf(value)
    }
}