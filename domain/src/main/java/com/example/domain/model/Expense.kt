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

data class Expense(
    val id: Long,
    val sender: UserData,
    val amount: Double,
    val date: Long,
    val note: String,
    val title: String,
    val group: Group
)

