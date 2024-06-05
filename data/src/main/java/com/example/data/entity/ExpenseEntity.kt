package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["spenderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spenderId"])]
)
data class ExpenseEntity(
    val spenderId: Long,
    val amount: Double,
    val description: String?,
    val date: Long,


    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0
)
