package com.example.kanakunote.data_layer.crossreference


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.data.entity.UserEntity

@Entity(
    primaryKeys = ["user1Id","user2Id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["user1Id"]
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["user2Id"]
        )
    ],
    indices = [Index(value = ["user1Id"]),Index(value = ["user2Id"])]
)
data class FriendsConnectionCrossRef(
    val user1Id: Long,
    val user2Id: Long
)