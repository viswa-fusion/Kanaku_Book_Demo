package com.example.domain.repository

import android.graphics.Bitmap
import com.example.domain.model.Group
import com.example.domain.model.GroupData
import com.example.domain.model.GroupSummery
import com.example.domain.repository.response.DataLayerResponse

interface GroupRepository {
    interface  Info{
        suspend fun insertGroup(group: GroupData): DataLayerResponse<Boolean>
        suspend fun retrieveUserGroupsByUserId(userId: Long): DataLayerResponse<List<Group>>
    }

    interface Profile{
        suspend fun saveProfileImage(groupId: Long, image: Bitmap?): DataLayerResponse<Boolean>
        suspend fun getProfilePhoto(groupId: Long): DataLayerResponse<Bitmap>
    }
}