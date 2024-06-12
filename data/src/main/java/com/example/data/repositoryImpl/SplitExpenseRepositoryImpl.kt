package com.example.data.repositoryImpl

import android.util.Log
import com.example.data.crossreference.ExpenseCrossRef
import com.example.data.dao.ExpenseDao
import com.example.data.dao.SplitDao
import com.example.data.dao.UserDao
import com.example.data.entity.ExpenseType
import com.example.data.util.toExpenseData
import com.example.data.util.toExpenseEntity
import com.example.data.util.toSplitEntity
import com.example.data.util.toSplitEntry
import com.example.data.util.toUserProfileSummery
import com.example.domain.model.ExpenseData
import com.example.domain.model.ExpenseEntry
import com.example.domain.model.SplitEntry
import com.example.domain.repository.SplitExpenseRepository
import com.example.domain.repository.response.DataLayerErrorCode
import com.example.domain.repository.response.DataLayerResponse
import com.example.kanakunote.data_layer.crossreference.SplitExpenseCrossRef
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SplitExpenseRepositoryImpl(private val expenseDao: ExpenseDao, private val splitDao: SplitDao, private val userDao: UserDao) :
    SplitExpenseRepository.GroupExpense, SplitExpenseRepository.FriendExpense {
    override suspend fun insertGroupExpenseWithSplits(
        groupId: Long,
        expense: ExpenseEntry,
        splits: List<SplitEntry>
    ): DataLayerResponse<Boolean> {
        return commonInsertionOfExpenseAndSplit(groupId,ExpenseType.GroupExpense,expense,splits)
    }

    override suspend fun getGroupExpenseWithSplitsByGroupId(groupId: Long): DataLayerResponse<List<ExpenseData>> {
        val data = commonGetOfExpenseAndSplit(groupId, ExpenseType.GroupExpense)
        Log.i("coroutineDataTest","data c: $data id = $groupId")
        return data
    }

    override suspend fun payForExpense(expenseId: Long, userId: Long): DataLayerResponse<Boolean> {
        return try{
            splitDao.makePaidForTheSplitOfExpenseId(expenseId, userId)
            DataLayerResponse.Success(true)
        }catch (e: Exception){
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    override suspend fun insertFriendExpenseWithSplits(
        connectionId: Long,
        expense: ExpenseEntry,
        splits: List<SplitEntry>
    ): DataLayerResponse<Boolean> {
        return commonInsertionOfExpenseAndSplit(connectionId,ExpenseType.FriendsExpense,expense,splits)
    }

    override suspend fun getFriendExpenseWithSplitsByConnectionId(connectionId: Long): DataLayerResponse<List<ExpenseData>> {

        val data = commonGetOfExpenseAndSplit(connectionId, ExpenseType.FriendsExpense)

        return data

    }


    private suspend fun commonGetOfExpenseAndSplit(
        id:Long,
        expenseType: ExpenseType
    ): DataLayerResponse<List<ExpenseData>> {
        return try{
            val result = expenseDao.getExpensesByTypeAndAssociatedId(expenseType, id)
            Log.i("coroutineDataTest","data result: $result")
            val defArray = mutableListOf<Deferred<ExpenseData>>()
            coroutineScope {
                result.forEach {
                    val data =  async{
                        val list = async { splitDao.getSplitsByExpenseId(it.expenseId) }
                        val user = async { userDao.getUserById(it.spenderId)?.toUserProfileSummery()!!}
                        val userData = user.await()
                        val listData = list.await()
                        it.toExpenseData(userData, listData.map{it.toSplitEntry()})
                    }
                    defArray.add(data)
                    Log.i("coroutineDataTest","data async: $data")
                }
            }
            val resultList = defArray.awaitAll()
            DataLayerResponse.Success(resultList)
        }catch (e:Exception){
            println(e.stackTrace)
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }
    private suspend fun commonInsertionOfExpenseAndSplit(
        id:Long,
        expenseType: ExpenseType,
        expense: ExpenseEntry,
        splits: List<SplitEntry>
    ): DataLayerResponse<Boolean>{
        val expenseId = expenseDao.insertExpense(expense.toExpenseEntity())
        return coroutineScope {
            val defArray = mutableListOf<Deferred<Boolean>>()
            splits.forEach {
                defArray.add(async {
                    val splitId = splitDao.insertSplit(it.toSplitEntity())
                    val crossRef = SplitExpenseCrossRef(expenseId, splitId)
                    try {
                        splitDao.insertSplitExpenseCrossRef(crossRef)
                        true
                    } catch (e: Exception) {
                        println(e.stackTrace)
                        false
                    }
                })
            }
            defArray.add(
                async {
                    val crossRef = ExpenseCrossRef(expenseType,id,expenseId)
                    try{
                        expenseDao.insertExpenseCrossRef(crossRef)
                        true
                    }catch (e: Exception){
                        println(e.stackTrace)
                        false
                    }
                }
            )
            val result = defArray.awaitAll()

            result.forEach{
                if (!it) return@coroutineScope DataLayerResponse.Error<Boolean>(DataLayerErrorCode.OPERATION_FAILED)
            }
            DataLayerResponse.Success(true)
        }
    }

}