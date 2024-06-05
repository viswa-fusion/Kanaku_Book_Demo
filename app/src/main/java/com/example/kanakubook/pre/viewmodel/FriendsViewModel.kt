package com.example.kanakubook.pre.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.ProfilePictureUseCase
import com.example.domain.usecase.UserUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.ImageDirectoryType
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.fragment.MultiUserPickListFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val useCase: UserUseCase.FriendsUseCase,
    private val profileUseCase: ProfilePictureUseCase
) : ViewModel() {

    var selectedList = mutableListOf<MultiUserPickListFragment.MySelectableUserData>()

    private val _friendsList =
        MutableLiveData<PresentationLayerResponse<List<UserProfileSummary>>>()
    val friendsList: LiveData<PresentationLayerResponse<List<UserProfileSummary>>> = _friendsList

    private val _addFriend = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val addFriend: LiveData<PresentationLayerResponse<Boolean>> = _addFriend

    fun addFriend(userId: Long, friendPhone: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _addFriend.postValue(useCase.addFriend(userId, friendPhone))
        }
    }

    fun addProfile(userId: Long, image: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            profileUseCase.addProfileImage(ImageDirectoryType.User(userId), image)
        }
    }

    suspend fun getProfile(userId: Long): Bitmap? {
        return when (val result = profileUseCase.getProfileImage(ImageDirectoryType.User(userId))) {
            is PresentationLayerResponse.Success -> {
                result.data
            }
            is PresentationLayerResponse.Error -> {
                null
            }
        }
    }

    fun getMyFriends(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.getMyFriends(userId)
            _friendsList.postValue(result)
        }
    }

    companion object {
        val FACTORY = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                val applicationDataInjection =
                    application!!.applicationContext as KanakuBookApplication
                val loginUseCase = applicationDataInjection.friendsUseCase
                val profilePictureUseCase = applicationDataInjection.profilePictureUseCase

                return FriendsViewModel(loginUseCase, profilePictureUseCase) as T
            }
        }
    }
}
//import android.graphics.Bitmap
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import com.example.domain.model.UserProfileSummery
//import com.example.domain.usecase.ProfilePictureUseCase
//import com.example.domain.usecase.UserUseCase
//import com.example.domain.usecase.response.PresentationLayerResponse
//import com.example.domain.usecase.util.ImageDirectoryType
//import com.example.kanakubook.pre.KanakuBookApplication
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//class FriendsViewModel(
//    private val useCase: UserUseCase.FriendsUseCase,
//    private val profileUseCase: ProfilePictureUseCase
//) : ViewModel() {
//
//    private val _friendsList = MutableStateFlow<PresentationLayerResponse<List<UserProfileSummery>>>(PresentationLayerResponse.Loading)
//    val friendsList: StateFlow<PresentationLayerResponse<List<UserProfileSummery>>> = _friendsList
//
//    private val _addFriend = MutableLiveData<PresentationLayerResponse<Boolean>>()
////    val addFriend: LiveData<PresentationLayerResponse<Boolean>> = _addFriend
//
//    fun addFriend(userId: Long, friendPhone: Long) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result = useCase.addFriend(userId, friendPhone)
//                _addFriend.postValue(result)
//            } catch (e: Exception) {
//                _addFriend.postValue(PresentationLayerResponse.Error(e))
//            }
//        }
//    }
//
//    fun addProfile(userId: Long, image: Bitmap) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                profileUseCase.addProfileImage(ImageDirectoryType.User(userId), image)
//            } catch (e: Exception) {
//                // Handle error if needed
//            }
//        }
//    }
//
//    suspend fun getProfile(userId: Long): Bitmap? {
//        return when (val result = profileUseCase.getProfileImage(ImageDirectoryType.User(userId))) {
//            is PresentationLayerResponse.Success -> result.data
//            is PresentationLayerResponse.Error -> null
////            is PresentationLayerResponse.Loading -> null // Handle loading state if needed
//        }
//    }
//
//    fun getMyFriends(userId: Long) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result = useCase.getMyFriends(userId)
//                _friendsList.value = result
//            } catch (e: Exception) {
//                _friendsList.value = PresentationLayerResponse.Error(e)
//            }
//        }
//    }
//
//    companion object {
//        val FACTORY = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
//
//                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
//                val applicationDataInjection =
//                    application!!.applicationContext as KanakuBookApplication
//                val loginUseCase = applicationDataInjection.friendsUseCase
//                val profilePictureUseCase = applicationDataInjection.profilePictureUseCase
//
//                return FriendsViewModel(loginUseCase, profilePictureUseCase) as T
//            }
//        }
//    }
//}
