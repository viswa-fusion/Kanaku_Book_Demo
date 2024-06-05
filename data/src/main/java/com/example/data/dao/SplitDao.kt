package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.SplitEntity
import com.example.kanakunote.data_layer.crossreference.SplitExpenseCrossRef

@Dao
interface SplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: SplitEntity): Long

    @Query("SELECT * FROM SplitEntity WHERE splitId IN (SELECT splitId FROM splitExpenseCrossRef WHERE expenseId = :expenseId)")
    suspend fun getSplitsByExpenseId(expenseId: Long): List<SplitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitExpenseCrossRef(splitExpenseCrossRef: SplitExpenseCrossRef)
//    @Query("SELECT * FROM splitExpenseCrossRef WHERE expenseId = :expenseId")
//    suspend fun getSplitExpenseCrossRefByExpenseId(expenseId: Long): List<SplitExpenseCrossRef>
}