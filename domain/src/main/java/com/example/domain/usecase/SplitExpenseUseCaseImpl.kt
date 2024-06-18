package com.example.domain.usecase

import com.example.domain.Converters.ActivityType
import com.example.domain.Converters.PaidStatus
import com.example.domain.helper.CryptoHelper
import com.example.domain.helper.DateTimeHelper
import com.example.domain.model.ActivityModelEntry
import com.example.domain.model.CommonGroupWIthAmountData
import com.example.domain.model.ExpenseData
import com.example.domain.model.ExpenseEntry
import com.example.domain.model.SplitEntry
import com.example.domain.repository.GroupRepository
import com.example.domain.repository.SplitExpenseRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.response.ActivityRepository
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.response.PresentationLayerResponse
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SplitExpenseUseCaseImpl(
    private val updateGroup: GroupRepository.Info,
    private val updateConnection: UserRepository.Info,
    private val splitGroupExpenseRepository: SplitExpenseRepository.GroupExpense,
    private val splitFriendsExpenseRepository: SplitExpenseRepository.FriendExpense,
    private val activityRepository: ActivityRepository
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
                val activity = ActivityModelEntry(
                    -1,
                    CryptoHelper.decrypt(ownerId),
                    ActivityType.ADD_EXPENSE,
                    DateTimeHelper.getCurrentTime(),
                    null,
                    groupId = CryptoHelper.decrypt(groupId)
                )
                coroutineScope {
                    launch { activityRepository.insertActivity(activity) }
                }

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
        spenderId: Long,
        expenseId: Long,
        userId: Long
    ): PresentationLayerResponse<Boolean> {
        return when(val result = splitGroupExpenseRepository.payForExpense(CryptoHelper.decrypt(expenseId),CryptoHelper.decrypt(userId))){
            is DataLayerResponse.Success -> {
                val time = DateTimeHelper.getCurrentTime()
                val activityPay = ActivityModelEntry(
                    -1L,
                    CryptoHelper.decrypt(userId),
                    ActivityType.PAY_FOR_EXPENSE,
                    time,
                    null,
                    expenseId = CryptoHelper.decrypt(expenseId)
                )
                val activitySplitMemberPay = ActivityModelEntry(
                    -1L,
                    CryptoHelper.decrypt(spenderId),
                    ActivityType.SPLIT_MEMBER_PAY,
                    time,
                    null,
                    friendId = CryptoHelper.decrypt(userId),
                    expenseId = CryptoHelper.decrypt(expenseId)
                )
                coroutineScope {
                    launch {  activityRepository.insertActivity(activityPay) }
                    launch { activityRepository.insertActivity(activitySplitMemberPay) }
                }
                PresentationLayerResponse.Success(result.data)
            }

            is DataLayerResponse.Error -> {
                PresentationLayerResponse.Error(result.errorCode.toString())
            }
        }
    }

    override suspend fun getCommonGroupWithFriendIdWithCalculatedAmount(
        userId: Long,
        friendId: Long
    ): PresentationLayerResponse<List<CommonGroupWIthAmountData>> {
        val decryptedUserId = CryptoHelper.decrypt(userId)
        val decryptedFriendId = CryptoHelper.decrypt(friendId)
        return when(val result = updateGroup.getCommonGroupsWithCalculatedBalance(decryptedUserId, decryptedFriendId)){
            is DataLayerResponse.Success -> {
                val encryptResult = result.data.map {
                    it.copy(
                        group = it.group.copy(id = CryptoHelper.encrypt(it.group.id)),
                        members = it.members.map {
                            it.copy(userId = CryptoHelper.encrypt(it.userId))
                        }
                    )
                }
                PresentationLayerResponse.Success(encryptResult)
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
        var friendId  = -1L
        val listOfSplit = splitList.map {
            if (it.first != ownerId) friendId = CryptoHelper.decrypt(it.first)
            SplitEntry(
                CryptoHelper.decrypt(it.first),
                it.second,
                if(it.first == ownerId) PaidStatus.Paid else PaidStatus.UnPaid
            )
        }

        return when(val result = splitFriendsExpenseRepository.insertFriendExpenseWithSplits(CryptoHelper.decrypt(connectionId), expense, listOfSplit)){
            is DataLayerResponse.Success -> {
                updateConnection.updateConnectionActiveTime(CryptoHelper.decrypt(connectionId),DateTimeHelper.getCurrentTime())
                val activity = ActivityModelEntry(
                    -1,
                    CryptoHelper.decrypt(ownerId),
                    ActivityType.ADD_EXPENSE,
                    DateTimeHelper.getCurrentTime(),
                    null,
                    friendId = friendId,
                    connectionId = CryptoHelper.decrypt(connectionId)
                )
                coroutineScope {
                    launch { activityRepository.insertActivity(activity) }
                }
                PresentationLayerResponse.Success(result.data)
            }
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