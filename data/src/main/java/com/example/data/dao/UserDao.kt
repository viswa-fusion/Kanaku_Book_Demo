package com.example.data.dao

import androidx.room.*
import com.example.data.crossreference.FriendsConnectionCrossRef
import com.example.data.entity.UserEntity
import com.example.data.relation.FriendsWithConnectionId

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity): Int

    @Delete
    suspend fun deleteUser(user: UserEntity): Int

    @Query("SELECT * FROM users WHERE userid = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("""SELECT * FROM users
            WHERE userId NOT IN (
            SELECT user1Id FROM FriendsConnectionCrossRef WHERE user2Id = :userId
            UNION
            SELECT user2Id FROM FriendsConnectionCrossRef WHERE user1Id = :userId)
            AND userId != :userId""")
    suspend fun getAllUsersExceptMyFriends(userId: Long): List<UserEntity>

    @Query("Select * FROM users Where phone = :phone")
    suspend fun getUserByCredentials(phone: Long): UserEntity?

    @Query("Select userId FROM users Where phone = :phone")
    suspend fun getUserIdByPhone(phone: Long): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFriendsConnection(connection: FriendsConnectionCrossRef)

    @Query("SELECT * FROM users WHERE userId IN (SELECT user2Id FROM FriendsConnectionCrossRef WHERE user1Id = :userId) OR userId IN(SELECT user1Id FROM FriendsConnectionCrossRef WHERE user2Id = :userId)")
    suspend fun getFriendsOfUser(userId: Long): List<UserEntity>

    @Query(
        """SELECT * FROM FriendsConnectionCrossRef
                INNER JOIN users ON FriendsConnectionCrossRef.user1Id = users.userId
                WHERE FriendsConnectionCrossRef.user2Id = :userId 
                UNION 
                SELECT * FROM FriendsConnectionCrossRef 
                INNER JOIN users ON FriendsConnectionCrossRef.user2Id = users.userId 
                WHERE FriendsConnectionCrossRef.user1Id = :userId"""
    )
    suspend fun getFriendsWithConnectionId(userId: Long): List<FriendsWithConnectionId>


    @Query("SELECT * FROM users WHERE userId IN (SELECT userId FROM GroupMemberCrossRef WHERE userId = :groupId)")
    suspend fun getUsersByGroupId(groupId: Long): List<UserEntity>
}
