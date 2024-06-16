package com.example.domain.usecase

import android.graphics.Bitmap
import com.example.domain.Converters.ActivityType
import com.example.domain.helper.CryptoHelper
import com.example.domain.helper.DateTimeHelper
import com.example.domain.model.ActivityModelEntry
import com.example.domain.model.GroupData
import com.example.domain.model.GroupEntry
import com.example.domain.repository.response.ActivityRepository
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.delegate.GroupRepositoryFunctionProviderDelegate
import com.example.domain.usecase.response.PresentationLayerResponse
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class GroupUseCaseImpl(
    private val repo: GroupRepositoryFunctionProviderDelegate,
    private val activityRepo: ActivityRepository
    ) :
    UserUseCase.GroupUseCase {
    override suspend fun fetchLoggedInUserGroups(userId: Long): PresentationLayerResponse<List<GroupData>> {

        return when(val result = repo.retrieveUserGroupsByUserId(CryptoHelper.decrypt(userId))) {
            is DataLayerResponse.Success -> {
                val data = result.data.map {
                    it.copy(
                        id = CryptoHelper.encrypt(it.id),
                        members = it.members.map {userData ->
                            userData.copy(
                                userId = CryptoHelper.encrypt(userData.userId)
                            )
                        })
                }
                val sortedData = data.sortedByDescending { it.lastActiveTime }
                PresentationLayerResponse.Success(sortedData)
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error("Authentication Failed")
        }
    }

    override suspend fun addMembers(
        userId:Long,
        groupId: Long,
        memberList: List<Long>
    ): PresentationLayerResponse<Boolean> {
        val decryptGroupId = CryptoHelper.decrypt(groupId)
        val decryptMembersId = memberList.map {
            CryptoHelper.decrypt(it)
        }
        return when(val result = repo.addMembers(decryptGroupId,decryptMembersId)){
            is DataLayerResponse.Success -> {
                val activity = ActivityModelEntry(
                    -1L,
                    CryptoHelper.decrypt(userId),
                    ActivityType.ADD_MEMBER_TO_GROUP,
                    DateTimeHelper.getCurrentTime(),
                    null,
                    groupId = CryptoHelper.decrypt(groupId)
                )
                coroutineScope {
                    launch { activityRepo.insertActivity(activity) }
                }
                PresentationLayerResponse.Success(result.data)
            }
            is DataLayerResponse.Error -> {
                PresentationLayerResponse.Error(result.errorCode.toString())
            }
        }
    }

    override suspend fun addGroup(name: String, image: Bitmap?, createdBy: Long, memberList: List<Long>): PresentationLayerResponse<Boolean> {

        val decrypted = GroupEntry(
            name = name,
            profilePicture = image,
            createdBy = CryptoHelper.decrypt(createdBy),
            DateTimeHelper.getCurrentTime(),
            members = memberList.map {
                CryptoHelper.decrypt(it)
            }
        )
        return when (val result = repo.insertGroupEntry(decrypted)) {
            is DataLayerResponse.Success -> {
                val activity = ActivityModelEntry(
                    -1L,
                    CryptoHelper.decrypt(createdBy),
                    ActivityType.CREATE_GROUP,
                    DateTimeHelper.getCurrentTime(),
                    null,
                    groupId = result.data
                )
                coroutineScope {
                    launch { activityRepo.insertActivity(activity) }
                }

                PresentationLayerResponse.Success(true)
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error("failed to retrieve data")
        }
    }
}