package com.example.domain.repository

import com.example.domain.model.ExpenseData
import com.example.domain.model.ExpenseEntry
import com.example.domain.model.SplitEntry
import com.example.domain.repository.response.DataLayerResponse

interface SplitExpenseRepository {
    interface GroupExpense{
        suspend fun insertGroupExpenseWithSplits(groupId: Long, expense:ExpenseEntry, splits: List<SplitEntry>): DataLayerResponse<Boolean>
        suspend fun getGroupExpenseWithSplitsByGroupId(groupId: Long): DataLayerResponse<List<ExpenseData>>
    }

    interface FriendExpense{
        suspend fun insertFriendExpenseWithSplits(connectionId: Long, expense:ExpenseEntry, splits: List<SplitEntry>): DataLayerResponse<Boolean>
        suspend fun getFriendExpenseWithSplitsByConnectionId(connectionId: Long): DataLayerResponse<List<ExpenseData>>
    }
}