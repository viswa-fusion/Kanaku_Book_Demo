package com.example.kanakubook.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.model.ActivityModel
import com.example.domain.usecase.ActivityUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.presentation.KanakuBookApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val activityUseCase: ActivityUseCase
) : ViewModel() {

    private val _activityResponse =
        MutableLiveData<PresentationLayerResponse<List<ActivityModel>>>()
    val activityResponse: LiveData<PresentationLayerResponse<List<ActivityModel>>> =
        _activityResponse

    fun getAllMyActivity(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _activityResponse.postValue(activityUseCase.getAllMyActivity(userId))
        }
    }

    companion object {
        val FACTORY = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                val applicationDataInjection =
                    application!!.applicationContext as KanakuBookApplication
                val activityUseCase = applicationDataInjection.activityUseCase

                return ActivityViewModel(activityUseCase) as T
            }
        }
    }
}