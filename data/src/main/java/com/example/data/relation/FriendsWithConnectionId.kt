package com.example.data.relation

import androidx.room.Embedded
import com.example.data.entity.UserEntity
import com.example.domain.model.GroupData

//data class FriendsWithConnectionId(
//    @Embedded val userEntity: UserEntity,
//    val connectionId: Long
//)
data class FriendsWithConnectionId(
    @Embedded val userEntity: UserEntity,
    val connectionId: Long,
)


