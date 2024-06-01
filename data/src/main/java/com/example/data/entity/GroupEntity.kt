package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups"
)
data class GroupEntity(
    val groupName: String,
    val createdBy: Long,

    @PrimaryKey(autoGenerate = true)
    val groupId: Long = 0,
)