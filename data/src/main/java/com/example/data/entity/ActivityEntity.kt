package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.Converters.ActivityType

@Entity(tableName = "activity")
data class ActivityEntity(
    val userId: Long,
    val activityType: ActivityType,
    val timestamp: Long,
    val details: String?,
    val friendId: Long? = null,
    val groupId: Long? = null,
    val expenseId: Long? = null,

    @PrimaryKey(autoGenerate = true)
    val activityId: Long = 0,
)
