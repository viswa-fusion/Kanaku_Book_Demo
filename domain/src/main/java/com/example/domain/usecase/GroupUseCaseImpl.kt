package com.example.domain.usecase

import com.example.domain.helper.CryptoHelper
import com.example.domain.model.Group
import com.example.domain.model.GroupData
import com.example.domain.model.GroupSummery
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.delegate.GroupRepositoryFunctionProviderDelegate
import com.example.domain.usecase.response.PresentationLayerResponse


class GroupUseCaseImpl(private val repo: GroupRepositoryFunctionProviderDelegate) :
    UserUseCase.GroupUseCase {
    override suspend fun fetchLoggedInUserGroups(userId: Long): PresentationLayerResponse<List<Group>> {

        return when(val result = repo.retrieveUserGroupsByUserId(CryptoHelper.decrypt(userId))){
            is DataLayerResponse.Success -> {
                PresentationLayerResponse.Success(result.data.map {
                    it.copy(id = CryptoHelper.encrypt(it.id))
                })
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error("Authentication Failed")
        }
    }

    override suspend fun addGroup(group: GroupData): PresentationLayerResponse<Boolean> {

        val decrypted = group.copy(
            createdBy = CryptoHelper.decrypt(group.createdBy),
            members = listOf(1,6,4,3)
        )
//        decrypted.apply {
//            this.members.forEach {
//            CryptoHelper.decrypt(it)
//        }

        return when (val result = repo.insertGroup(decrypted)) {
            is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
            is DataLayerResponse.Error -> PresentationLayerResponse.Error("failed to retrieve data")
        }
    }
}