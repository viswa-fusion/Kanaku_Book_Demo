package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.enum.PaidStatus

@Entity
data class SplitEntity(
    val splitUserId: Long,
    val splitAmount: Double,
    val paidStatus: com.example.domain.enum.PaidStatus,

    @PrimaryKey(autoGenerate = true)
    val splitId: Long = 0
)