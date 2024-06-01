package com.example.domain.usecase.response

sealed interface PresentationLayerResponse<T> {
    data class Success<T>(val data: T) : PresentationLayerResponse<T>
    data class Error<T>(val message: String) : PresentationLayerResponse<T>
}