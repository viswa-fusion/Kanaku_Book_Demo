package com.example.domain.usecase

import android.graphics.Bitmap
import com.example.domain.helper.CryptoHelper
import com.example.domain.helper.DateTimeHelper
import com.example.domain.model.GroupData
import com.example.domain.model.GroupEntry
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.delegate.GroupRepositoryFunctionProviderDelegate
import com.example.domain.usecase.response.PresentationLayerResponse


class GroupUseCaseImpl(private val repo: GroupRepositoryFunctionProviderDelegate) :
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
            is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
            is DataLayerResponse.Error -> PresentationLayerResponse.Error("failed to retrieve data")
        }
    }
}