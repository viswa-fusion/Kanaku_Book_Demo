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
        suspend fun getAllKanakuBookUsers(userId: Long): PresentationLayerResponse<List<UserProfileSummary>>
    }
    interface GroupUseCase{
        suspend fun addGroup(name: String, image: Bitmap?, createdBy: Long, memberList: List<Long>): PresentationLayerResponse<Boolean>
        suspend fun fetchLoggedInUserGroups(userId: Long) : PresentationLayerResponse<List<GroupData>>
        suspend fun getGroupByGroupId(groupId: Long): PresentationLayerResponse<GroupData>
        suspend fun addMembers(userId:Long,groupId:Long,memberList: List<Long>): PresentationLayerResponse<Boolean>
    }

    interface FriendsUseCase{
        suspend fun addFriend(userId: Long, friendPhone : Long) : PresentationLayerResponse<Boolean>
        suspend fun getMyFriends(userId: Long): PresentationLayerResponse<List<UserProfileSummary>>
        suspend fun updateUser(userId: Long, userName: String?, dob:String?,profile:Bitmap?): PresentationLayerResponse<Boolean>
    }

    interface ActivityUseCase{

    }
}