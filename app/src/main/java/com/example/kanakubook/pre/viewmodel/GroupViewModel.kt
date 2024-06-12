package com.example.kanakubook.pre.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.DisplayShape
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.domain.model.ExpenseData
import com.example.domain.model.GroupData
import com.example.domain.model.GroupEntry
import com.example.domain.usecase.ProfilePictureUseCase
import com.example.domain.usecase.SplitExpenseUseCase
import com.example.domain.usecase.UserUseCase
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.ImageDirectoryType
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.util.ImageConversionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupViewModel(
    private val useCase: UserUseCase.GroupUseCase,
    private val profilePictureUseCase: ProfilePictureUseCase,
    private val expenseUseCase: SplitExpenseUseCase.GroupExpense
) : ViewModel() {


    private val _groupCreateResponse = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val groupCreateResponse: LiveData<PresentationLayerResponse<Boolean>> = _groupCreateResponse

    private val _getAllGroups = MutableLiveData<PresentationLayerResponse<List<GroupData>>>()
    val getAllGroups: LiveData<PresentationLayerResponse<List<GroupData>>> = _getAllGroups

    private val _groupExpenseCreateResponse = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val groupExpenseCreateResponse: LiveData<PresentationLayerResponse<Boolean>> =
        _groupExpenseCreateResponse

    private val _getAllGroupExpenseResponse = MutableLiveData< PresentationLayerResponse<List<ExpenseData>>>()
    val getAllGroupExpenseResponse: LiveData< PresentationLayerResponse<List<ExpenseData>>> =
        _getAllGroupExpenseResponse

    private val _addMembersResponse = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val addMembersResponse: LiveData<PresentationLayerResponse<Boolean>> = _addMembersResponse

    private val _payResponse = MutableLiveData<PresentationLayerResponse<Boolean>>()
    val payResponse : LiveData<PresentationLayerResponse<Boolean>> = _payResponse

    fun createExpense(
        groupId: Long,
        ownerId: Long,
        totalAmount: Double,
        note: String,
        splitList: List<Pair<Long, Double>>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _groupExpenseCreateResponse.postValue(
                expenseUseCase.addNewGroupExpense(
                    groupId,
                    ownerId,
                    totalAmount,
                    note,
                    splitList
                )
            )
        }
    }

    fun addMembers(groupId: Long, memberList: List<Long>){
        viewModelScope.launch(Dispatchers.IO) {
           _addMembersResponse.postValue(useCase.addMembers(groupId,memberList))
        }
    }

    fun getAllExpenseByGroupId(groupId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            val data = expenseUseCase.getGroupExpenseById(groupId)
            _getAllGroupExpenseResponse.postValue(data)
        }
    }


    fun createGroup(
        context: Context,
        userId: Long,
        groupName: String,
        imageUri: Uri?,
        memberList: List<Long>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("timeCheck", "time1: ${System.currentTimeMillis()}")
            val image = imageUri?.let { ImageConversionHelper.loadBitmapFromUri(context, imageUri) }
            Log.i("timeCheck", "time2: ${System.currentTimeMillis()}")

            val response = useCase.addGroup(
                groupName,
                image,
                userId,
                memberList
            )
            Log.i("timeCheck", "time3: ${System.currentTimeMillis()}")

            _groupCreateResponse.postValue(response)
        }
    }

    fun pay(expenseId: Long, userId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            _payResponse.postValue(expenseUseCase.payForExpense(expenseId,userId))
        }
    }

    fun getAllMyGroups(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.fetchLoggedInUserGroups(userId)
            _getAllGroups.postValue(result)
        }
    }

    suspend fun getProfile(groupId: Long): Bitmap? {
        val result = profilePictureUseCase.getProfileImage(ImageDirectoryType.Group(groupId))
        return when (result) {
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
                val groupExpenseUseCase = applicationDataInjection.groupExpenseUseCase

                return GroupViewModel(useCase, profileUseCase,groupExpenseUseCase) as T
            }
        }
    }

}