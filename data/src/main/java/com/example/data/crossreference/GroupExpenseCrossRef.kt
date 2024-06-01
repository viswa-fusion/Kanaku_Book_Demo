package com.example.kanakunote.data_layer.crossreference

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.GroupEntity

@Entity(
    primaryKeys = ["groupId", "expenseId"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"]), Index(value = ["expenseId"])]
)
data class GroupExpenseCrossRef(
    val groupId: Long,
    val expenseId: Long
)