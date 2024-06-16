package com.example.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.entity.ActivityEntity
import com.example.data.relation.ActivityRelation
import com.example.domain.Converters.ActivityType

@Dao
interface ActivityDao {

    @Insert
    suspend fun insertActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activity ORDER BY timestamp DESC")
    fun getAllActivities(): LiveData<List<ActivityEntity>>

    @Query("SELECT * FROM activity WHERE userId = :userId ORDER BY timestamp DESC")
    fun getActivitiesForUser(userId: Long): List<ActivityRelation>

    @Query("SELECT * FROM activity WHERE activityType = :activityType ORDER BY timestamp DESC")
    fun getActivitiesByType(activityType: ActivityType): LiveData<List<ActivityEntity>>

    @Query("SELECT * FROM activity WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getActivitiesBetweenDates(startDate: Long, endDate: Long): LiveData<List<ActivityEntity>>

    @Query("DELETE FROM activity")
    suspend fun deleteAllActivities()

    @Query("DELETE FROM activity WHERE userId = :userId")
    suspend fun deleteActivitiesByUserId(userId: Long)

    @Query("DELETE FROM activity WHERE activityType = :activityType")
    suspend fun deleteActivitiesByType(activityType: ActivityType)
}