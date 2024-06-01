package com.example.kanakubook.pre.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.model.Group
import com.example.domain.model.GroupData
import com.example.domain.usecase.ProfilePictureUseCase
import com.example.domain.usecase.UserUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.ImageDirectoryType
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.util.ImageConversionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupViewModel(
    private val useCase: UserUseCase.GroupUseCase,
    private val profilePictureUseCase: ProfilePictureUseCase
): ViewModel() {

    private val _groupCreateResponse = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val groupCreateResponse : LiveData<PresentationLayerResponse<Boolean>> = _groupCreateResponse

    private val _getAllGroups = MutableLiveData<PresentationLayerResponse<List<Group>>>()
    val getAllGroups : LiveData<PresentationLayerResponse<List<Group>>> = _getAllGroups

    fun createGroup(context: Context,userId: Long, groupName: String, imageUri: Uri?, memberList: List<Long>){
        viewModelScope.launch {
            val image = ImageConversionHelper.loadBitmapFromUri(context, imageUri)
            val response = useCase.addGroup(
                GroupData(
                    groupName,
                    image,
                    userId,
                    memberList
                )
            )
            _groupCreateResponse.postValue(response)
        }
    }

    fun getAllMyGroups(userId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.fetchLoggedInUserGroups(userId)
            _getAllGroups.postValue(result)
        }
    }

    suspend fun getProfile(groupId: Long): Bitmap?{
        val result = profilePictureUseCase.getProfileImage(ImageDirectoryType.Group(groupId))
        return when(result){
            is PresentationLayerResponse.Success -> {
                result.data
            }

            is PresentationLayerResponse.Error -> {
                null
            }
        }
    }



    companion object {
        val FACTORY = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                val applicationDataInjection =
                    application!!.applicationContext as KanakuBookApplication
                val useCase = applicationDataInjection.userUseCaseOfGroupUseCase
                val profileUseCase = applicationDataInjection.profilePictureUseCase

                return GroupViewModel(useCase,profileUseCase) as T
            }
        }
    }

}