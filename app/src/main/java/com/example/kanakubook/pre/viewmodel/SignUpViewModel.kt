package com.example.kanakubook.pre.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.usecase.SignUpUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.pre.KanakuBookApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SignUpViewModel (
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    private var _userId = MutableLiveData<PresentationLayerResponse<Long>>()
    val userId: LiveData<PresentationLayerResponse<Long>> = _userId

    fun signUp(name: String, phone: Long, password: String, repeatPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _userId.postValue(signUpUseCase.addUser(name, phone, password, repeatPassword))
            delay(500)
        }
    }

    companion object{
        val FACTORY = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                val applicationDataInjection = application!!.applicationContext as KanakuBookApplication
                val loginUseCase = applicationDataInjection.signUpUseCase

                return SignUpViewModel(loginUseCase)as T
            }
        }
    }
}