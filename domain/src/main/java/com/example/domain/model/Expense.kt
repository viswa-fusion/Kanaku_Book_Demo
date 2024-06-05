package com.example.domain.model

//data class OneOnOneExpense(
//    val id: Long,
//    val sender: UserData,
//    val receiver: UserData,
//    val amount: Double,
//    val date: Long,
//    val note: String,
//    val title: String
//)

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
