package com.example.kanakubook.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.UserUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.presentation.KanakuBookApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class UserViewModel(
    private val useCase: UserUseCase.CommonUserUseCase
) : ViewModel() {

    private val _userData = MutableLiveData<List<UserProfileSummary>>()
    val userData: LiveData<List<UserProfileSummary>> = _userData

    private val _allUserData =
        MutableLiveData<PresentationLayerResponse<List<UserProfileSummary>>>()
    val allUserData: LiveData<PresentationLayerResponse<List<UserProfileSummary>>> = _allUserData

    fun getUser(userId: List<Long>) {
        val members = mutableListOf<UserProfileSummary>()
        viewModelScope.launch(Dispatchers.IO) {
            val asyncData = userId.map {
                this.async { useCase.getUserById(it) }
            }
            val data = asyncData.awaitAll()
            data.forEach {
                when (it) {
                    is PresentationLayerResponse.Success -> {
                        members.add(it.data)
                    }

                    is PresentationLayerResponse.Error -> {
                        return@launch
                    }
                }
            }
            _userData.postValue(members)
        }
    }


    fun getAllKanakuBookUsers(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _allUserData.postValue(useCase.getAllKanakuBookUsers(userId))
        }
    }

    companion object {
        val FACTORY = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                val applicationDataInjection =
                    application!!.applicationContext as KanakuBookApplication
                val userUseCaseCommonUserUseCase =
                    applicationDataInjection.userUseCaseCommonUserUseCase

                return UserViewModel(userUseCaseCommonUserUseCase) as T
            }
        }
    }

}