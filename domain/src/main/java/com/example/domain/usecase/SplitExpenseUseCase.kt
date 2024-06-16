package com.example.domain.usecase

import com.example.domain.model.CommonGroupWIthAmountData
import com.example.domain.model.ExpenseData
import com.example.domain.usecase.response.PresentationLayerResponse

interface SplitExpenseUseCase {

    interface GroupExpense{
        suspend fun addNewGroupExpense(
            groupId: Long,
            ownerId:Long,
            totalAmount:Double,
            note: String,
            splitList: List<Pair<Long,Double>>
        ): PresentationLayerResponse<Boolean>

        suspend fun getGroupExpenseById(groupId: Long): PresentationLayerResponse<List<ExpenseData>>

        suspend fun payForExpense(spenderId:Long, expenseId: Long, userId: Long): PresentationLayerResponse<Boolean>
    }

    interface FriendsExpense{

        suspend fun addNewFriendsExpense(
            connectionId: Long,
            ownerId:Long,
            totalAmount:Double,
            note: String,
            splitList: List<Pair<Long,Double>>
        ): PresentationLayerResponse<Boolean>
        suspend fun getFriendsExpenseById(connectionId: Long): PresentationLayerResponse<List<ExpenseData>>
        suspend fun payForExpense(spenderId:Long, expenseId: Long, userId: Long): PresentationLayerResponse<Boolean>

        suspend fun getCommonGroupWithFriendIdWithCalculatedAmount(userId: Long, friendId: Long): PresentationLayerResponse<List<CommonGroupWIthAmountData>>
    }
}