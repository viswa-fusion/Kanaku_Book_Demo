package com.example.kanakunote.data_layer.crossreference

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.UserEntity

@Entity(
    primaryKeys = ["userId", "expenseId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["expenseId"])]
)
data class UserExpenseCrossRef(
    val userId: Long,
    val expenseId: Long
)

