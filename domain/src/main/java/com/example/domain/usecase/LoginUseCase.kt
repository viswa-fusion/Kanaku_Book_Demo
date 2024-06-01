package com.example.domain.usecase

import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse

interface LoginUseCase {
    suspend fun authenticateUser(phone: Long, password: String): PresentationLayerResponse<UserProfileSummary>
}