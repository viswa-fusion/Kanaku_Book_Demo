package com.example.data.util

import com.example.data.entity.ExpenseEntity
import com.example.domain.model.GroupSummery
import com.example.domain.model.UserEntryData
import com.example.domain.model.UserProfileData
import com.example.domain.model.UserProfileSummary
import com.example.data.entity.GroupEntity
import com.example.data.entity.SplitEntity
import com.example.data.relation.GroupWithMembers
import com.example.data.entity.UserEntity
import com.example.data.relation.CommonGroupWithAmount
import com.example.domain.model.CommonGroupWIthAmountData
import com.example.domain.model.ExpenseData
import com.example.domain.model.ExpenseEntry
import com.example.domain.model.GroupData
import com.example.domain.model.GroupEntry
import com.example.domain.model.SplitEntry


internal fun UserEntryData.toUserEntity(password: String = "fakePassword"): UserEntity {
    return UserEntity(
        this.name,
        this.phone,
        password
    )
}

internal fun UserEntity.toUserProfileSummery(connectionId: Long? = null, pay:Double=0.0,get:Double=0.0): UserProfileSummary {
    return UserProfileSummary(
        this.userId,
        this.name,
        this.phone
    ).apply {
        this.connectionId = connectionId
        this.pay = pay
        this.get = get
    }
}

internal fun UserEntity.toUserProfileData(
    profilePhotoFilePath: String,
    amountToGet: Double,
    amountToGive: Double
): UserProfileData {
    return UserProfileData(
        this.userId,
        this.name,
        this.phone,
        amountToGet,
        amountToGive
    )
}

internal fun GroupWithMembers.toGroupSummery(profilePhotoFilePath: String): GroupSummery {
    return GroupSummery(
        this.group.groupId,
        this.group.groupName,
        this.group.createdBy,
        this.group.lastActive
    )
}

internal fun CommonGroupWithAmount.toCommonGroupWithAmountData(): CommonGroupWIthAmountData{
    return CommonGroupWIthAmountData(
        GroupSummery(
            this.groupData.groupId,
            this.groupData.groupName,
            this.groupData.createdBy,
            this.groupData.lastActive
        ),
        this.give,
        this.get

    )
}


internal fun GroupData.toGroupEntity(time: Long): GroupEntity {
    return GroupEntity(
        this.name,
        this.createdBy,
        time
    )
}

internal fun GroupEntry.toGroupEntity(): GroupEntity {
    return GroupEntity(
        this.name,
        this.createdBy,
        this.lastActiveTime
    )
}

internal fun GroupWithMembers.toGroupData(): GroupData {
    return GroupData(
        this.group.groupId,
        this.group.groupName,
        this.group.createdBy,
        this.group.lastActive,
        this.members.map {
            it.toUserProfileSummery()
        }
    )
}

internal fun ExpenseEntry.toExpenseEntity(): ExpenseEntity{
    return ExpenseEntity(
        this.spenderId,
        this.totalAmount,
        this.note,
        this.date
    )
}

internal fun SplitEntry.toSplitEntity(): SplitEntity{
    return SplitEntity(
        this.splitUserId,
        this.splitAmount,
        this.paidStatus
    )
}

internal fun ExpenseEntity.toExpenseData(spenderData: UserProfileSummary, listOfSplit: List<SplitEntry>): ExpenseData{
    return ExpenseData(
        this.expenseId,
        spenderData,
        this.amount,
        this.date,
        this.description,
        listOfSplit
    )
}

internal fun SplitEntity.toSplitEntry(): SplitEntry{
    return SplitEntry(
        this.splitUserId,
        this.splitAmount,
        this.paidStatus
    )
}

internal fun UserEntity.toUserProfileData(): UserProfileData {
    return UserProfileData(
        this.userId,
        this.name,
        this.phone,
        this.amountToGet,
        this.amountToGive
    )
}