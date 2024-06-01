package com.example.domain.repository

import android.graphics.Bitmap
import com.example.domain.model.UserData
import com.example.domain.model.UserProfileSummary
import com.example.domain.repository.response.DataLayerResponse

interface UserRepository {

    interface Authentication {
        suspend fun authenticateUser(phone: Long): DataLayerResponse<Pair<UserProfileSummary, String>>
    }

    interface Info{
        suspend fun insertUser(userData: UserData, password: String): DataLayerResponse<Long>
        suspend fun updateUser(userData: UserData): DataLayerResponse<Boolean>
        suspend fun deleteUser(userData: UserData): DataLayerResponse<Boolean>
        suspend fun getUserIdByPhone(phone: Long): DataLayerResponse<Long>
        suspend fun addFriend(userId: Long, friendId: Long): DataLayerResponse<Boolean>
        suspend fun getFriendsOfUser(userId: Long): DataLayerResponse<List<UserProfileSummary>>
        suspend fun getUserProfileSummeryByUserId(userId: Long): DataLayerResponse<UserProfileSummary>
        suspend fun getAllUsers(): DataLayerResponse<List<UserProfileSummary>>
    }



    interface UserProfile{
        suspend fun saveUserProfileImage(userId: Long, image: Bitmap): DataLayerResponse<Boolean>
        suspend fun getUserProfilePhoto(userId: Long): DataLayerResponse<Bitmap>
    }


}



