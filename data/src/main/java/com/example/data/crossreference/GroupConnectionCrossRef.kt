package com.example.kanakunote.data_layer.crossreference

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.GroupEntity
import com.example.data.entity.UserEntity

@Entity(
    primaryKeys = ["groupId","userId"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"]),Index(value = ["userId"])]
)
data class GroupConnectionCrossRef(
    val groupId: Long,
    val userId: Long
)