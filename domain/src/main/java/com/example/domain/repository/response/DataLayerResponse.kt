package com.example.domain.repository.response

sealed interface DataLayerResponse<T> {
    data class Success<T>(val data: T) : DataLayerResponse<T>
    data class Error<T>(val errorCode: DataLayerErrorCode) : DataLayerResponse<T>
}