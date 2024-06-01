package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.entity.GroupEntity
import com.example.data.entity.UserEntity

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["spenderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"]
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["friendId"]
        )
    ],
    indices = [Index(value = ["payerId"])]
)
internal data class ExpenseEntity(
    val spenderId: Long,
    val amount: Double,
    val description: String,
    val date: Long,
    val groupId: Long?,
    val friendId: Long?,

    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0
)