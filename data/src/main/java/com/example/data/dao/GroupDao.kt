package com.example.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.crossreference.GroupMemberCrossRef
import com.example.data.entity.GroupEntity
import com.example.data.relation.CommonGroupWithAmount
import com.example.data.relation.GroupWithMembers
import com.example.data.relation.GroupWithMembers1

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(groupMembers: List<GroupMemberCrossRef>)

    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun getGroupEntity(groupId: Long): GroupEntity

@Transaction
@Query("""
    SELECT 
        groups.groupId,
        groups.groupName,
        groups.createdBy,
        groups.lastActive,
        COALESCE(SUM(CASE WHEN s1.userId = :userId THEN s1.splitAmount ELSE 0.0 END), 0.0) AS pay,  
        COALESCE(SUM(CASE WHEN s2.userId != :userId THEN s2.splitAmount ELSE 0.0 END), 0.0) AS get 
    FROM 
        groups  
        LEFT JOIN GroupMemberCrossRef AS gmc ON groups.groupId = gmc.groupId
        LEFT JOIN ExpenseCrossRef AS a ON groups.groupId = a.associatedId AND a.expenseType = 'GroupExpense' 
        LEFT JOIN expenses AS e1 ON a.expenseId = e1.expenseId AND e1.spenderId != :userId
        LEFT JOIN expenses AS e2 ON a.expenseId = e2.expenseId AND e2.spenderId = :userId
        LEFT JOIN SplitExpenseCrossRef AS c1 ON e1.expenseId = c1.expenseId 
        LEFT JOIN SplitExpenseCrossRef AS c2 ON e2.expenseId = c2.expenseId  
        LEFT JOIN SplitEntity AS s1 ON c1.splitId = s1.splitId AND s1.paidStatus = 'UnPaid' AND e1.spenderId != :userId
        LEFT JOIN SplitEntity AS s2 ON c2.splitId = s2.splitId AND s2.paidStatus = 'UnPaid' AND e2.spenderId = :userId
    WHERE 
        gmc.userId = :userId
    GROUP BY 
        groups.groupId, groups.groupName, groups.createdBy, groups.lastActive
""")
suspend fun getGroupsWithMembersAndBalances(userId: Long): List<GroupWithMembers>




    @Transaction
    @Query("UPDATE groups SET lastActive = :time WHERE groupId = :groupId")
    suspend fun updateGroupActiveTime(groupId: Long, time: Long)



    @Transaction
    @Query(
        """
    SELECT 
        b.groupId, b.groupName, b.createdBy, b.lastActive,
        COALESCE(SUM(CASE WHEN splits.paidStatus = 'UnPaid' AND splits.userId = :userId THEN splits.splitAmount ELSE 0.0 END), 0.0) AS give,
        COALESCE(SUM(CASE WHEN splits.paidStatus = 'UnPaid' AND splits.userId = :friendId THEN splits.splitAmount ELSE 0.0 END), 0.0) AS get
    FROM 
        (
            SELECT 
                my.groupId
            FROM 
                GroupMemberCrossRef AS my
            WHERE 
                my.userId LIKE :userId
        ) AS my
    INNER JOIN 
        (
            SELECT 
                friend.groupId
            FROM 
                GroupMemberCrossRef AS friend
            WHERE 
                friend.userId LIKE :friendId
        ) AS friend ON my.groupId = friend.groupId
    INNER JOIN 
        GROUPS AS b ON my.groupId = b.groupId
    LEFT JOIN 
        expensecrossref AS ec ON b.groupId = ec.associatedId AND ec.expenseType = "GroupExpense"
    LEFT JOIN 
        splitexpensecrossref AS sec ON ec.expenseId = sec.expenseId
    LEFT JOIN 
        splitentity AS splits ON sec.splitId = splits.splitId
    GROUP BY 
        b.groupId, b.groupName, b.createdBy, b.lastActive
"""
    )
    suspend fun getCommonGroupsWithCalculatedBalance(
        userId: Long,
        friendId: Long
    ): List<CommonGroupWithAmount>

}


data class GroupWithPayAndGet(
    @Embedded val group: GroupEntity,
    val pay: Double,
    val get: Double
)