package com.example.domain.usecase


import com.example.domain.model.Group
import com.example.domain.model.GroupData
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse

interface UserUseCase {
    interface GroupUseCase{
        suspend fun addGroup(group: GroupData): PresentationLayerResponse<Boolean>
        suspend fun fetchLoggedInUserGroups(userId: Long) : PresentationLayerResponse<List<Group>>
    }

    interface FriendsUseCase{
        suspend fun addFriend(userId: Long, friendPhone : Long) : PresentationLayerResponse<Boolean>
        suspend fun getMyFriends(userId: Long): PresentationLayerResponse<List<UserProfileSummary>>
    }

    interface ActivityUseCase{

    }
}