package com.example.kanakubook.delegation

import com.example.data.repositoryImpl.RepositoryImpl
import com.example.data.repositoryImpl.UserRepositoryImpl
import com.example.domain.repository.GroupRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.UserUseCase
import com.example.domain.usecase.delegate.GroupRepositoryFunctionProviderDelegate


class GroupRepositoryFunctionProviderDelegateImpl (
    groupRepository: RepositoryImpl,
    userRepository: UserRepositoryImpl
):GroupRepositoryFunctionProviderDelegate,
        GroupRepository.Info by groupRepository