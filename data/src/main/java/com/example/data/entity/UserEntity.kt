package com.example.data.entity


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["phone"], unique = true)])
data class UserEntity(
    val name: String,
    val phone: Long,
    val password: String,
    val amountToGet: Double = 0.0,
    val amountToGive: Double = 0.0,
){
    @PrimaryKey(autoGenerate = true)
    var userId: Long = 0L
}


