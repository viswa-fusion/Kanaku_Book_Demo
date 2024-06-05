package com.example.domain.model

import com.example.domain.Converters.PaidStatus

data class SplitEntry(
    val splitUserId: Long,
    val splitAmount: Double,
    val paidStatus: PaidStatus,
)
