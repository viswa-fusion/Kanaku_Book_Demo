package com.example.domain.repository.response

import com.example.domain.model.ActivityModel
import com.example.domain.model.ActivityModelEntry

interface ActivityRepository {
    suspend fun insertActivity(activity: ActivityModelEntry): DataLayerResponse<Boolean>
    suspend fun getAllMyActivity(userId: Long): DataLayerResponse<List<ActivityModel>>

}