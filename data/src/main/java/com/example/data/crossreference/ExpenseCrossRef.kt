package com.example.data.crossreference

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ExpenseType

@Entity(
    primaryKeys = ["expenseId", "associatedId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["expenseId"])]
)
data class ExpenseCrossRef(
    val expenseType: ExpenseType,
    val associatedId: Long,
    val expenseId: Long,
)

