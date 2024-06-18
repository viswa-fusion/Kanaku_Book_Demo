package com.example.domain.model


import android.graphics.Bitmap
import com.example.domain.Converters.ActivityType


data class ActivityModelEntry(
    val activityId: Long,
    val userId: Long,
    val activityType: ActivityType,
    val timestamp: Long,
    val details: String?,
    val friendId: Long? = null,
    val groupId: Long? = null,
    val expenseId: Long? = null,
    val connectionId: Long? = null
)
data class ActivityModel(
    val activityId: Long,
    val user: UserProfileSummary,
    val activityType: ActivityType,
    val timestamp: Long,
    val details: String?,
    val friend: UserProfileSummary? = null,
    val group: GroupSummery? = null,
    val expense: ExpenseData? = null,
    val connectionId: Long? = null
){
    var mainImage: Bitmap? = null
    var subImage: Bitmap? = null
}
