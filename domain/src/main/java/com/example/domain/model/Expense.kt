package com.example.domain.model


data class ExpenseEntry(
    val spenderId: Long,
    val totalAmount: Double,
    val date: Long,
    val note: String?
)

data class ExpenseData(
    val expenseId: Long,
    val spender: UserProfileSummary,
    val totalAmount: Double,
    val date: Long,
    val note: String?,
    val listOfSplits: List<SplitEntry>
)
