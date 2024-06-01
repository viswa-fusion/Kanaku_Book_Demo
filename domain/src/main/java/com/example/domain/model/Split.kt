package com.example.domain.model

import com.example.domain.enum.PaidStatus

data class Split(
    val id: Long,
    val expense: Expense,
    val userData: UserData,
    val amount: Double,
    val paidStatus: com.example.domain.enum.PaidStatus
)