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

    @Query("SELECT * FROM groups WHERE groupId IN (SELECT groupId FROM groupmembercrossref WHERE userId = :userId)")
    suspend fun getGroupsOfUser(userId: Long): List<GroupEntity>

    @Transaction
    @Query("SELECT * FROM groups WHERE groupId IN (SELECT groupId FROM GroupMemberCrossRef WHERE userId = :userId)")
    suspend fun getGroupsWithMembersByUserId(userId: Long): List<GroupWithMembers>

    //@Transaction
//@Query("""SELECT groups.*,
//        COALESCE(SUM(CASE WHEN expenses.spenderId != :userId THEN SplitEntity.splitAmount ELSE 0.0 END), 0.0) AS pay,
//        COALESCE(SUM(CASE WHEN expenses.spenderId = :userId THEN SplitEntity.splitAmount ELSE 0.0 END), 0.0) AS get
//        FROM groups
//        LEFT JOIN GroupMemberCrossRef ON groups.groupId = GroupMemberCrossRef.groupId
//        LEFT JOIN ExpenseCrossRef ON groups.groupId = ExpenseCrossRef.associatedId
//        LEFT JOIN expenses ON ExpenseCrossRef.expenseId = expenses.expenseId
//        LEFT JOIN SplitExpenseCrossRef ON ExpenseCrossRef.expenseId = SplitExpenseCrossRef.expenseId
//        LEFT JOIN SplitEntity ON SplitExpenseCrossRef.splitId = SplitEntity.splitId
//        WHERE GroupMemberCrossRef.userId = :userId
//        GROUP BY groups.groupId""")
//suspend fun getGroupsWithMembersAndBalances(userId: Long): List<GroupWithMembers1>
//    @Transaction
//    @Query(
//        """SELECT b.groupId, b.groupName, b.createdBy, b.lastActive,
//        COALESCE(SUM(CASE WHEN splits.paidStatus = 'UnPaid' AND splits.userId != :userId THEN splits.splitAmount ELSE 0.0 END), 0.0) AS give,
//        COALESCE(SUM(CASE WHEN splits.paidStatus = 'UnPaid' AND splits.userId = :userId THEN splits.splitAmount ELSE 0.0 END), 0.0) AS get
//        FROM groups as b
//        LEFT JOIN ExpenseCrossRef ON b.groupId = ExpenseCrossRef.associatedId
//        LEFT JOIN expenses ON ExpenseCrossRef.expenseId = expenses.expenseId AND expenseType == "GroupExpense"
//        LEFT JOIN SplitExpenseCrossRef ON ExpenseCrossRef.expenseId = SplitExpenseCrossRef.expenseId
//        LEFT JOIN SplitEntity as splits ON SplitExpenseCrossRef.splitId = splits.splitId
//        WHERE groupId IN (SELECT groupId FROM GroupMemberCrossRef WHERE userId = :userId) """
//    )
//    suspend fun getGroupsWithMembersAndBalances(userId: Long): List<GroupWithMembers1>
    @Query("SELECT groups.groupId, groups.groupName, groups.createdBy, groups.lastActive," +
            "78.0 AS pay, " +
            "87.0 AS get " +
            "FROM groups " +
            "WHERE groupId IN (SELECT groupId FROM GroupMemberCrossRef WHERE userId = :userId)")
    suspend fun getGroupsWithMembers(userId: Long): List<GroupWithMembers1>



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