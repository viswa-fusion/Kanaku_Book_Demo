package com.example.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.SplitEntity
import com.example.kanakunote.data_layer.crossreference.SplitExpenseCrossRef

data class ExpenseWithSplit(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "expenseId",
        entityColumn = "splitId",
        associateBy = Junction(SplitExpenseCrossRef::class)
    ) val splits: List<SplitEntity>
)

