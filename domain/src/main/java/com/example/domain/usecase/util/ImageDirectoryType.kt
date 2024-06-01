package com.example.domain.usecase.util

sealed interface ImageDirectoryType {
    data class User(val userId: Long): ImageDirectoryType
    data class Group(val groupId: Long): ImageDirectoryType
}