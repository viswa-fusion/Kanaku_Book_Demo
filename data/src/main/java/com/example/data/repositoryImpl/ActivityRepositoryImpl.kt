package com.example.data.repositoryImpl

import com.example.data.dao.ActivityDao
import com.example.data.dao.GroupDao
import com.example.data.dao.SplitDao
import com.example.data.dao.UserDao
import com.example.data.util.toActivityEntity
import com.example.data.util.toActivityModelEntry
import com.example.data.util.toSplitEntry
import com.example.data.util.toUserProfileSummery
import com.example.domain.model.ActivityModel
import com.example.domain.model.ActivityModelEntry
import com.example.domain.repository.response.ActivityRepository
import com.example.domain.repository.response.DataLayerErrorCode
import com.example.domain.repository.response.DataLayerResponse
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ActivityRepositoryImpl(
    private val activityDao: ActivityDao,
    private val splitDao: SplitDao,
    private val userDao: UserDao,
    private val groupDao: GroupDao
) : ActivityRepository {
    override suspend fun insertActivity(activity: ActivityModelEntry): DataLayerResponse<Boolean> {
        return try {
            activityDao.insertActivity(activity.toActivityEntity())
            DataLayerResponse.Success(true)
        } catch (e: Exception) {
            DataLayerResponse.Error(DataLayerErrorCode.INSERT_FAILED)
        }
    }

    override suspend fun getAllMyActivity(userId: Long): DataLayerResponse<List<ActivityModel>> {

        return try {
            val dataResult = mutableListOf<Deferred<ActivityModel>>()
            coroutineScope {
                val result = activityDao.getActivitiesForUser(userId)
                result.map {
                    val data = async {
                        val list =
                            async { it.expense?.let { _ -> splitDao.getSplitsByExpenseId(it.expense.expenseId) } }
                        val user =
                            async { userDao.getUserById(it.user.userId)?.toUserProfileSummery()!! }
                        val group =
                            async { it.group?.let { _ -> groupDao.getGroupEntity(it.group.groupId) } }
                        val userData = user.await()
                        val listData = list.await()
                        val groupData = group.await()
                        it.toActivityModelEntry(
                            userData,
                            listData?.map { it.toSplitEntry() },
                            groupData
                        )
                    }
                    dataResult.add(data)
                }
            }
            val finalResult = dataResult.awaitAll()
            DataLayerResponse.Success(finalResult)
        } catch (e: Exception) {
            println(e.stackTrace)
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

}