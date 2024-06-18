package com.example.kanakubook.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.model.CommonGroupWIthAmountData
import com.example.domain.model.ExpenseData
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.ProfilePictureUseCase
import com.example.domain.usecase.SplitExpenseUseCase
import com.example.domain.usecase.UserUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.ImageDirectoryType
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.fragment.MultiUserPickListFragment
import com.example.kanakubook.util.ImageConversionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val useCase: UserUseCase.FriendsUseCase,
    private val profileUseCase: ProfilePictureUseCase,
    private val expenseUseCase: SplitExpenseUseCase.FriendsExpense
) : ViewModel() {

    var profileImage: Bitmap? = null
    var profileUri: Uri? = null
    var selectedList = mutableListOf<MultiUserPickListFragment.MySelectableUserData>()
    var listOfMySelectableUserData = emptyList<MultiUserPickListFragment.MySelectableUserData>()
    var listOfMySelectableUserProfileSummary = emptyList<UserProfileSummary>()
    var isNotFirstTimeValidation = false
    var userId: Long? = null
    var phone: Long? = null
    var name: String? = null
    var searchText: String = ""

    private val _friendsList =
        MutableLiveData<PresentationLayerResponse<List<UserProfileSummary>>>()
    val friendsList: LiveData<PresentationLayerResponse<List<UserProfileSummary>>> = _friendsList

    private val _addFriend = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val addFriend: LiveData<PresentationLayerResponse<Boolean>> = _addFriend

    private val _friendsExpenseCreateResponse =
        MutableLiveData<PresentationLayerResponse<Boolean>>()
    val friendsExpenseCreateResponse: LiveData<PresentationLayerResponse<Boolean>> =
        _friendsExpenseCreateResponse

    private val _getAllFriendsExpenseResponse =
        MutableLiveData<PresentationLayerResponse<List<ExpenseData>>>()
    val getAllFriendsExpenseResponse: LiveData<PresentationLayerResponse<List<ExpenseData>>> =
        _getAllFriendsExpenseResponse

    private val _commonGroupResponse =
        MutableLiveData<PresentationLayerResponse<List<CommonGroupWIthAmountData>>>()
    val commonGroupResponse: LiveData<PresentationLayerResponse<List<CommonGroupWIthAmountData>>> =
        _commonGroupResponse

    private val _payResponse = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val payResponse: LiveData<PresentationLayerResponse<Boolean>> = _payResponse

    private val _userUpdateResponse = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val userUpdateResponse: LiveData<PresentationLayerResponse<Boolean>> = _userUpdateResponse


    fun createExpense(
        groupId: Long,
        ownerId: Long,
        totalAmount: Double,
        note: String,
        splitList: List<Pair<Long, Double>>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _friendsExpenseCreateResponse.postValue(
                expenseUseCase.addNewFriendsExpense(
                    groupId,
                    ownerId,
                    totalAmount,
                    note,
                    splitList
                )
            )
        }
    }

    fun pay(spenderId: Long, expenseId: Long, userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _payResponse.postValue(expenseUseCase.payForExpense(spenderId, expenseId, userId))
        }
    }

    fun getCommonGroupWithFriendIdWithCalculatedAmount(userId: Long, friendId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val result =
                expenseUseCase.getCommonGroupWithFriendIdWithCalculatedAmount(userId, friendId)

            _commonGroupResponse.postValue(result)
        }
    }

    fun updateUser(userId: Long, userName: String?, dob: String?, profile: Uri?, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = profile?.let { ImageConversionHelper.loadBitmapFromUri(context, profile) }
            _userUpdateResponse.postValue(useCase.updateUser(userId, userName, dob, bitmap))
        }
    }

    fun getAllExpenseByConnectionId(connectionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = expenseUseCase.getFriendsExpenseById(connectionId)
            _getAllFriendsExpenseResponse.postValue(data)
        }
    }

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
                val friendsExpense = applicationDataInjection.friendsExpenseUseCase

                return FriendsViewModel(loginUseCase, profilePictureUseCase, friendsExpense) as T
            }
        }
    }
}