package com.example.domain.usecase

import com.example.domain.usecase.response.PresentationLayerResponse

interface SignUpUseCase {
    suspend fun addUser(name: String, phone: Long, dob: String?, password: String, repeatPassword: String) : PresentationLayerResponse<Long>
}