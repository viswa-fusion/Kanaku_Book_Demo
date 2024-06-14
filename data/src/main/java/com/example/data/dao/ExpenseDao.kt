package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.crossreference.ExpenseCrossRef
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ExpenseType

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("SELECT * FROM expenses WHERE spenderId = :spenderId")
    suspend fun getExpensesBySpenderId(spenderId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    suspend fun getExpenseById(expenseId: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE expenseId IN (SELECT expenseId FROM ExpenseCrossRef  WHERE expenseType = :expenseType AND associatedId = :associatedId)")
    suspend fun getExpensesByTypeAndAssociatedId(expenseType: ExpenseType, associatedId: Long): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCrossRef(expenseCrossRef: ExpenseCrossRef)

    @Query("SELECT * FROM expenseCrossRef WHERE expenseType = :expenseType AND associatedId = :belongingId")
    suspend fun getExpenseCrossRefByTypeAndBelongingId(expenseType: ExpenseType, belongingId: Long): ExpenseCrossRef?
}



