package com.example.kanakunote.data_layer.crossreference

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.SplitEntity

@Entity(
    primaryKeys = ["expenseId","splitId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"]
        ),
        ForeignKey(
            entity = SplitEntity::class,
            parentColumns = ["splitId"],
            childColumns = ["splitId"]
        )
    ],
    indices = [Index(value = ["expenseId"]),Index(value = ["splitId"])]
)
data class SplitExpenseCrossRef(
    val expenseId: Long,
    val splitId: Long,
)