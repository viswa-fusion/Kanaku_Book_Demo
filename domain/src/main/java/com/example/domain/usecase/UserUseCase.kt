package com.example.domain.usecase


import android.graphics.Bitmap
import android.net.Uri
import com.example.domain.model.GroupData
import com.example.domain.model.GroupEntry
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse

interface UserUseCase {
    interface CommonUserUseCase{
        suspend fun getUserById(userId: Long): PresentationLayerResponse<UserProfileSummary>
    }
    interface GroupUseCase{
        suspend fun addGroup(name: String, image: Bitmap?, createdBy: Long, memberList: List<Long>): PresentationLayerResponse<Boolean>
        suspend fun fetchLoggedInUserGroups(userId: Long) : PresentationLayerResponse<List<GroupData>>
    }

    interface FriendsUseCase{
        suspend fun addFriend(userId: Long, friendPhone : Long) : PresentationLayerResponse<Boolean>
        suspend fun getMyFriends(userId: Long): PresentationLayerResponse<List<UserProfileSummary>>
    }

    interface ActivityUseCase{

    }
}