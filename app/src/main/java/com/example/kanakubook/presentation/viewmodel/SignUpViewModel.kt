package com.example.kanakubook.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.usecase.SignUpUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.presentation.KanakuBookApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SignUpViewModel (
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    var isNotFirstTimeValidation = false
    private var _userId = MutableLiveData<PresentationLayerResponse<Long>>()
    val userId: LiveData<PresentationLayerResponse<Long>> = _userId

    fun signUp(name: String, phone: Long, dob: String?, password: String, repeatPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val value = signUpUseCase.addUser(name, phone, dob, password, repeatPassword)
            withContext(Dispatchers.Main){
                _userId.value = value
            }

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