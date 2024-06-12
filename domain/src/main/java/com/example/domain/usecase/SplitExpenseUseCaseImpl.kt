package com.example.domain.usecase

import android.provider.ContactsContract.Contacts.Data
import com.example.domain.Converters.PaidStatus
import com.example.domain.helper.CryptoHelper
import com.example.domain.helper.DateTimeHelper
import com.example.domain.model.ExpenseData
import com.example.domain.model.ExpenseEntry
import com.example.domain.model.SplitEntry
import com.example.domain.repository.GroupRepository
import com.example.domain.repository.SplitExpenseRepository
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.response.PresentationLayerResponse

class SplitExpenseUseCaseImpl(
    private val updateGroup: GroupRepository.Info,
    private val splitGroupExpenseRepository: SplitExpenseRepository.GroupExpense,
    private val splitFriendsExpenseRepository: SplitExpenseRepository.FriendExpense,
) : SplitExpenseUseCase.GroupExpense, SplitExpenseUseCase.FriendsExpense {
    override suspend fun addNewGroupExpense(
        groupId: Long,
        ownerId: Long,
        totalAmount: Double,
        note: String,
        splitList: List<Pair<Long, Double>>
    ): PresentationLayerResponse<Boolean> {
        val time = DateTimeHelper.getCurrentTime()
        val expense = ExpenseEntry(
            CryptoHelper.decrypt(ownerId),
            totalAmount,
            time,
            note
        )
        val listOfSplit = splitList.map {
            SplitEntry(
                CryptoHelper.decrypt(it.first),
                it.second,
                if(it.first == ownerId) PaidStatus.Paid else PaidStatus.UnPaid
            )
        }

        return when(val result = splitGroupExpenseRepository.insertGroupExpenseWithSplits(CryptoHelper.decrypt(groupId), expense, listOfSplit)){
            is DataLayerResponse.Success -> {
                updateGroup.updateGroupActiveTime(CryptoHelper.decrypt(groupId), time)
                PresentationLayerResponse.Success(result.data)
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
        }
    }

    override suspend fun getGroupExpenseById(groupId: Long): PresentationLayerResponse<List<ExpenseData>> {
        return when(val result = splitGroupExpenseRepository.getGroupExpenseWithSplitsByGroupId(CryptoHelper.decrypt(groupId))){
            is DataLayerResponse.Success -> {
                val encryptData = result.data.map {
                    it.copy(
                        expenseId = CryptoHelper.encrypt(it.expenseId),
                        spender = it.spender.copy(userId = CryptoHelper.encrypt(it.spender.userId)),
                        listOfSplits = it.listOfSplits.map {splitData->
                            splitData.copy(splitUserId = CryptoHelper.encrypt(splitData.splitUserId))
                        }
                    )
                }
                val sortedData = encryptData.sortedByDescending { it.date }
                PresentationLayerResponse.Success(sortedData)
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
        }
    }

    override suspend fun payForExpense(
        expenseId: Long,
        userId: Long
    ): PresentationLayerResponse<Boolean> {
        return when(val result = splitGroupExpenseRepository.payForExpense(CryptoHelper.decrypt(expenseId),CryptoHelper.decrypt(userId))){
            is DataLayerResponse.Success -> {
                PresentationLayerResponse.Success(result.data)
            }

            is DataLayerResponse.Error -> {
                PresentationLayerResponse.Error(result.errorCode.toString())
            }
        }
    }

    override suspend fun addNewFriendsExpense(
        connectionId: Long,
        ownerId: Long,
        totalAmount: Double,
        note: String,
        splitList: List<Pair<Long, Double>>
    ): PresentationLayerResponse<Boolean> {
        val expense = ExpenseEntry(
            CryptoHelper.decrypt(ownerId),
            totalAmount,
            DateTimeHelper.getCurrentTime(),
            note
        )
        val listOfSplit = splitList.map {
            SplitEntry(
                CryptoHelper.decrypt(it.first),
                it.second,
                if(it.first == ownerId) PaidStatus.Paid else PaidStatus.UnPaid
            )
        }

        return when(val result = splitFriendsExpenseRepository.insertFriendExpenseWithSplits(CryptoHelper.decrypt(connectionId), expense, listOfSplit)){
            is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
            is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
        }
    }

    override suspend fun getFriendsExpenseById(connectionId: Long): PresentationLayerResponse<List<ExpenseData>> {

        return when(val result = splitFriendsExpenseRepository.getFriendExpenseWithSplitsByConnectionId(CryptoHelper.decrypt(connectionId))){
            is DataLayerResponse.Success -> {
                val encryptData = result.data.map {
                    it.copy(
                        expenseId = CryptoHelper.encrypt(it.expenseId),
                        spender = it.spender.copy(userId = CryptoHelper.encrypt(it.spender.userId)),
                        listOfSplits = it.listOfSplits.map {splitData->
                            splitData.copy(splitUserId = CryptoHelper.encrypt(splitData.splitUserId))
                        }

                    )

                }
                val sortedData = encryptData.sortedByDescending { it.date }
                PresentationLayerResponse.Success(sortedData)
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
        }
    }
}