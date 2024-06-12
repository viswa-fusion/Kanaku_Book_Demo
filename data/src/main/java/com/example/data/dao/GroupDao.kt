package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.crossreference.GroupMemberCrossRef
import com.example.data.entity.GroupEntity
import com.example.data.relation.GroupWithMembers

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(groupMembers: List<GroupMemberCrossRef>)

    @Query("SELECT * FROM groups WHERE groupId IN (SELECT groupId FROM groupmembercrossref WHERE userId = :userId)")
    fun getGroupsOfUser(userId: Long): List<GroupEntity>

    @Transaction
    @Query("SELECT * FROM groups WHERE groupId IN (SELECT groupId FROM GroupMemberCrossRef WHERE userId = :userId)")
    fun getGroupsWithMembersByUserId(userId: Long): List<GroupWithMembers>

    @Transaction
    @Query("UPDATE groups SET lastActive = :time WHERE groupId = :groupId")
    fun updateGroupActiveTime(groupId: Long, time: Long)
}

