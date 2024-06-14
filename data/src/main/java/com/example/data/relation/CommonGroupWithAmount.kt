package com.example.data.relation

import androidx.room.Embedded
import com.example.data.entity.GroupEntity

data class CommonGroupWithAmount(
    @Embedded val groupData: GroupEntity,
    val give:Double,
    val get:Double
)