package com.example.domain.usecase

import com.example.domain.model.ActivityModel
import com.example.domain.usecase.response.PresentationLayerResponse

interface ActivityUseCase {
    suspend fun getAllMyActivity(userId: Long): PresentationLayerResponse<List<ActivityModel>>
}