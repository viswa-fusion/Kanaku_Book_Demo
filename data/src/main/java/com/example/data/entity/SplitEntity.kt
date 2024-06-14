package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.Converters.PaidStatus

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class SplitEntity(
    @ColumnInfo(name = "userId")
    var splitUserId: Long,
    var splitAmount: Double,
    val paidStatus: PaidStatus,

    @PrimaryKey(autoGenerate = true)var splitId: Long = 0,
)
