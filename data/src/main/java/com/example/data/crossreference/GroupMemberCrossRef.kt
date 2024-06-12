package com.example.data.crossreference

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.GroupEntity
import com.example.data.entity.UserEntity

@Entity(
    primaryKeys = ["groupId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"]
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"]
        )
    ],
    indices = [Index(value = ["groupId"]), Index(value = ["userId"])]
)
data class GroupMemberCrossRef(
    val groupId: Long,
    val userId: Long
)