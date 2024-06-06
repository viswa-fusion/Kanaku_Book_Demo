package com.example.data.relation

import androidx.room.Embedded
import com.example.data.entity.UserEntity

data class FriendsWithConnectionId(
    @Embedded val userEntity: UserEntity,
    val connectionId: Long
)