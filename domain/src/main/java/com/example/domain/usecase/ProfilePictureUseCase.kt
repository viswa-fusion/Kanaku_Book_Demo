package com.example.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.ImageDirectoryType
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface ProfilePictureUseCase {
    suspend fun addProfileImage(
        imageDirectoryType: ImageDirectoryType,
        image: Bitmap,
    ): PresentationLayerResponse<Boolean>
    suspend fun getProfileImage(
        imageDirectoryType: ImageDirectoryType
    ): PresentationLayerResponse<Bitmap?>
}