package com.example.domain.model

import android.graphics.Bitmap

data class CommonGroupWIthAmountData(
    val group: GroupSummery,
    val pay: Double,
    val get: Double,
    val members: List<UserProfileSummary>
){
    var profile: Bitmap? = null
}