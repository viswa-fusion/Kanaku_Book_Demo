package com.example.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.entity.ActivityEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.GroupEntity
import com.example.data.entity.UserEntity

data class ActivityRelation(
    @Embedded val activity: ActivityEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )val user: UserEntity,
    @Relation(
        parentColumn = "friendId",
        entityColumn = "userId"
    )val friend: UserEntity?,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "groupId"
    ) val group: GroupEntity?,
    @Relation(
        parentColumn = "expenseId",
        entityColumn = "expenseId"
    ) val expense: ExpenseEntity?
    )