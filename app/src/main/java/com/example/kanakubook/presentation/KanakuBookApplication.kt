package com.example.kanakubook.presentation

import android.app.Application
import com.example.data.dao.ActivityDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.UserDao
import com.example.data.database.ApplicationDatabase
import com.example.data.repositoryImpl.RepositoryImpl
import com.example.data.repositoryImpl.UserRepositoryImpl
import com.example.domain.repository.GroupRepository
import com.example.domain.repository.UserRepository
import com.example.domain.usecase.GroupUseCaseImpl
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.ProfilePictureUseCase
import com.example.domain.usecase.SignUpUseCase
import com.example.domain.usecase.UserUseCase
import com.example.domain.usecase.UserUseCaseImpl
import com.example.kanakubook.StorageHelperImpl
import com.example.data.dao.GroupDao
import com.example.data.dao.SplitDao
import com.example.data.repositoryImpl.ActivityRepositoryImpl
import com.example.data.repositoryImpl.SplitExpenseRepositoryImpl
import com.example.domain.repository.SplitExpenseRepository
import com.example.domain.repository.response.ActivityRepository
import com.example.domain.usecase.ActivityUseCase
import com.example.domain.usecase.SplitExpenseUseCase
import com.example.domain.usecase.SplitExpenseUseCaseImpl
import com.example.kanakunote.data_layer.dao.ProfilePhotoDao

class KanakuBookApplication : Application() {

    companion object{
        val PREF_IS_USER_LOGIN = "PREF_LOGIN_BOOLEAN_KEY"
        val PREF_USER_ID = "PREF_LOGIN_USER_ID"
        val PREF_DEFAULT_DATA_INJECTED ="PREF_DEFAULT_DATA_INJECTED"
    }

    private val userUseCaseImpl : UserUseCaseImpl by lazy {
        UserUseCaseImpl(
            userInfoRepository,
            userAuthenticationRepository,
            groupRepositoryProfilePhoto,
            userProfileRepository,
            activityRepository
        )
    }
    private val userDao : UserDao by lazy {
        val db = ApplicationDatabase.getDatabase(this)
        db.getMyUserDao()
    }

    private val groupDao : GroupDao by lazy{
        val db = ApplicationDatabase.getDatabase(this)
        db.getMyGroupDao()
    }

    private val profilePhotoDao: ProfilePhotoDao by lazy {
        val db = ApplicationDatabase.getDatabase(this)
        db.getMyProfilePhotoDao()
    }

    private val expenseDao: ExpenseDao by lazy {
        val db = ApplicationDatabase.getDatabase(this)
        db.getMyExpenseDao()
    }
    private val splitDao: SplitDao by lazy {
        val db = ApplicationDatabase.getDatabase(this)
        db.getMySplitDao()
    }
    private val activityDao: ActivityDao by lazy {
        val db = ApplicationDatabase.getDatabase(this)
        db.getMyActivityDao()
    }

    private val storageHelperImpl : StorageHelperImpl by lazy {
        StorageHelperImpl(this)
    }

    private val userRepositoryImpl: UserRepositoryImpl by lazy {
        UserRepositoryImpl(userDao,profilePhotoDao, storageHelperImpl)
    }

    private val userInfoRepository : UserRepository.Info by lazy {
        userRepositoryImpl
    }
    private val userProfileRepository : UserRepository.UserProfile by lazy {
        userRepositoryImpl
    }
    private val userAuthenticationRepository : UserRepository.Authentication by lazy {
        userRepositoryImpl
    }

    private val groupRepositoryImpl :RepositoryImpl by lazy {
        RepositoryImpl(groupDao, profilePhotoDao, storageHelperImpl)
    }

    private val groupRepository: GroupRepository.Info by lazy {
        groupRepositoryImpl
    }

    private val groupRepositoryProfilePhoto : GroupRepository.Profile  by lazy {
        groupRepositoryImpl
    }

    private val activityRepository : ActivityRepository  by lazy {
        ActivityRepositoryImpl(activityDao,splitDao,userDao,groupDao)
    }

    private val groupUseCaseImpl: GroupUseCaseImpl by lazy {
        GroupUseCaseImpl(groupRepositoryImpl,activityRepository)
    }
    private val splitExpenseRepositoryImpl : SplitExpenseRepositoryImpl by lazy {
        SplitExpenseRepositoryImpl(expenseDao,splitDao,userDao)
    }

    private val groupExpenseRepository :SplitExpenseRepository.GroupExpense by lazy {
        splitExpenseRepositoryImpl
    }

    private val friendsExpenseRepository :SplitExpenseRepository.FriendExpense by lazy {
        splitExpenseRepositoryImpl
    }

    private val splitExpenseUseCaseImpl: SplitExpenseUseCaseImpl by lazy {
        SplitExpenseUseCaseImpl(groupRepository,userRepositoryImpl,groupExpenseRepository,friendsExpenseRepository,activityRepository)
    }

    val userUseCaseOfGroupUseCase: UserUseCase.GroupUseCase by lazy { groupUseCaseImpl }

    val signUpUseCase: SignUpUseCase by lazy { userUseCaseImpl }


    val loginUseCase: LoginUseCase by lazy { userUseCaseImpl }

    val friendsUseCase: UserUseCase.FriendsUseCase by lazy { userUseCaseImpl }

    val profilePictureUseCase: ProfilePictureUseCase by lazy { userUseCaseImpl }

    val userUseCaseCommonUserUseCase: UserUseCase.CommonUserUseCase by lazy { userUseCaseImpl }

    val groupExpenseUseCase : SplitExpenseUseCase.GroupExpense by lazy { splitExpenseUseCaseImpl }

    val friendsExpenseUseCase : SplitExpenseUseCase.FriendsExpense by lazy { splitExpenseUseCaseImpl }

    val activityUseCase : ActivityUseCase by lazy { userUseCaseImpl }
}