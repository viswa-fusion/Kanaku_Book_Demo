package com.example.domain.repository

import android.graphics.Bitmap
import com.example.domain.model.UserEntryData
import com.example.domain.model.UserProfileData
import com.example.domain.model.UserProfileSummary
import com.example.domain.repository.response.DataLayerResponse
import java.sql.Timestamp

interface UserRepository {

    interface Authentication {
        suspend fun authenticateUser(phone: Long): DataLayerResponse<Pair<UserProfileSummary, String>>
        suspend fun loggedUserByUserId(userId: Long): DataLayerResponse<UserProfileData>
    }

    interface Info {
        suspend fun insertUser(
            userEntryData: UserEntryData,
            password: String
        ): DataLayerResponse<Long>

        suspend fun updateUser(
            userId: Long,
            username: String?,
            dob: Long?
        ): DataLayerResponse<Boolean>

        suspend fun getUser(userId: Long): DataLayerResponse<UserProfileSummary>
        suspend fun deleteUser(userEntryData: UserEntryData): DataLayerResponse<Boolean>
        suspend fun getUserIdByPhone(phone: Long): DataLayerResponse<Long>
        suspend fun addFriend(
            userId: Long,
            friendId: Long,
            timestamp: Long
        ): DataLayerResponse<Long>

        suspend fun getFriendsOfUser(userId: Long): DataLayerResponse<List<UserProfileSummary>>
        suspend fun checkPhoneNumberExist(phone: Long): DataLayerResponse<Boolean>
        suspend fun getUserProfileSummeryByUserId(userId: Long): DataLayerResponse<UserProfileSummary>
        suspend fun getAllUsersExceptMyFriends(userId: Long): DataLayerResponse<List<UserProfileSummary>>
        suspend fun updateConnectionActiveTime(connectionId: Long, timestamp: Long)
    }


    interface UserProfile {
        suspend fun saveUserProfileImage(userId: Long, image: Bitmap): DataLayerResponse<Boolean>
        suspend fun getUserProfilePhoto(userId: Long): DataLayerResponse<Bitmap>
    }


}



