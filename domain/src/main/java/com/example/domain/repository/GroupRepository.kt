package com.example.domain.repository

import android.graphics.Bitmap
import com.example.domain.model.GroupData
import com.example.domain.model.GroupEntry
import com.example.domain.repository.response.DataLayerResponse
import java.sql.Time

interface GroupRepository {
    interface  Info{
        suspend fun insertGroupEntry(group: GroupEntry): DataLayerResponse<Boolean>
        suspend fun retrieveUserGroupsByUserId(userId: Long): DataLayerResponse<List<GroupData>>
        suspend fun updateGroupActiveTime(groupId: Long,time: Long)
    }

    interface Profile{
        suspend fun saveProfileImage(groupId: Long, image: Bitmap?): DataLayerResponse<Boolean>
        suspend fun getProfilePhoto(groupId: Long): DataLayerResponse<Bitmap>
    }
}