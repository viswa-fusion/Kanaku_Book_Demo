package com.example.kanakubook.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.model.UserProfileData
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.presentation.KanakuBookApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginViewModel (
    private val useCase: LoginUseCase
) : ViewModel() {

    var isNotFirstTimeValidation = false

    private val _userDataDetail = MutableLiveData<PresentationLayerResponse<UserProfileSummary>>()
    val userDataDetails: LiveData<PresentationLayerResponse<UserProfileSummary>> = _userDataDetail

    private val _loggedUserProfile = MutableLiveData<PresentationLayerResponse<UserProfileData>>()
    val loggedUserProfile = _loggedUserProfile

    fun authenticateUser(phone: Long, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.authenticateUser(phone, password)
            _userDataDetail.postValue(result)
        }
    }

    fun  getLoggedUser(userId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.loggedUserByUserId(userId)
            _loggedUserProfile.postValue(result)
        }
    }

    companion object{
        val FACTORY = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                val applicationDataInjection = application!!.applicationContext as KanakuBookApplication
                val loginUseCase = applicationDataInjection.loginUseCase

                return LoginViewModel(loginUseCase) as T
            }
        }
    }
}
