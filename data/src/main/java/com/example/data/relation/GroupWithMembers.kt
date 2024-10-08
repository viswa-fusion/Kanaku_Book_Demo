package com.example.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.data.crossreference.GroupMemberCrossRef
import com.example.data.entity.GroupEntity
import com.example.data.entity.UserEntity

data class GroupWithMembers(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "userId",
        associateBy = Junction(GroupMemberCrossRef::class)
    ) val members: List<UserEntity>,
    val pay: Double = 0.0,
    val get: Double = 0.0
)

data class GroupWithMembersOnly(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "userId",
        associateBy = Junction(GroupMemberCrossRef::class)
    ) val members: List<UserEntity>
)
