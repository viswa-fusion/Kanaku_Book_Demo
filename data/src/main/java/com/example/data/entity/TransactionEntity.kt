package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.entity.UserEntity

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["receiverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["payerId"]), Index(value = ["receiverId"])]
)
data class TransactionEntity(
    val senderId: Long,
    val receiverId: Long,
    val amount: Double,
    val date: Long,

    @PrimaryKey(autoGenerate = true)
    val paymentId: Long = 0
)