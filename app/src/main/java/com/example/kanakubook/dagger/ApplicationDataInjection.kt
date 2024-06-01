//package com.example.kanakubook.dagger
//
//import android.app.Application
//import com.example.data.dao.UserDao
//import com.example.data.database.ApplicationDatabase
//import com.example.data.repositoryImpl.GroupRepositoryImpl
//import com.example.data.repositoryImpl.UserRepositoryImpl
//import com.example.data.util.StorageHelper
//import com.example.domain.repository.GroupRepository
//import com.example.domain.repository.UserRepository
//import com.example.domain.usecase.LoginUseCase
//import com.example.domain.usecase.SignUpUseCase
//import com.example.domain.usecase.UserUseCaseImpl
//import com.example.kanakubook.StorageHelperImpl
//import com.example.data.dao.GroupDao
//import com.example.kanakunote.data_layer.dao.ProfilePhotoDao
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object ApplicationDataInjection : Application() {
//
//    @Provides
//    @Singleton
//    fun providesDb(): ApplicationDatabase = ApplicationDatabase.getDatabase(this)
//
//    @Provides
//    @Singleton
//    fun providesUserDao(db:ApplicationDatabase):UserDao = db.getMyUserDao()
//
//    @Provides
//    @Singleton
//    fun providesGroupDao(db:ApplicationDatabase):GroupDao = db.getMyGroupDao()
//
//    @Provides
//    @Singleton
//    fun providesProfilePhotoDao(db:ApplicationDatabase):ProfilePhotoDao = db.getMyProfilePhotoDao()
//
//    @Provides
//    @Singleton
//    fun providesStorageHelper():StorageHelper = StorageHelperImpl(this)
//
//    @Provides
//    @Singleton
//    fun providesUserRepository(userDao: UserDao,profilePhotoDao: ProfilePhotoDao,storageHelper: StorageHelper) : UserRepository = UserRepositoryImpl(userDao,profilePhotoDao,storageHelper)
//
//    @Provides
//    @Singleton
//    fun providesGroupRepository(groupDao: GroupDao,profilePhotoDao: ProfilePhotoDao,storageHelper: StorageHelper) : GroupRepository = GroupRepositoryImpl(groupDao, profilePhotoDao, storageHelper)
//
//    @Provides
//    @Singleton
//    fun providesSignInUseCase(userRepository: UserRepository, groupRepository: GroupRepository.GroupProfile): SignUpUseCase = UserUseCaseImpl(userRepository,groupRepository)
//
//    @Provides
//    @Singleton
//    fun providesLoginUseCase(userRepository: UserRepository, groupRepository: GroupRepository.GroupProfile):LoginUseCase = UserUseCaseImpl(userRepository,groupRepository)
//
////    private fun userUseCaseImpl() : UserUseCaseImpl {
////        UserUseCaseImpl(
////            userRepository(),
////            groupRepositoryOfGroupProfile()
////        )
////    }
////    private fun userDao() {
////        val db = ApplicationDatabase.getDatabase(this)
////        db.getMyUserDao()
////    }
////
////    private fun groupDao() {
////        val db = ApplicationDatabase.getDatabase(this)
////        db.getMyGroupDao()
////    }
////
////    private fun profilePhotoDao() {
////        val db = ApplicationDatabase.getDatabase(this)
////        db.getMyProfilePhotoDao()
////    }
////
////    private fun storageHelper() {
////        StorageHelperImpl(this)
////    }
////
////    private fun groupRepositoryImpl() {
////        GroupRepositoryImpl(groupDao(), profilePhotoDao, storageHelper)
////    }
////
////    private fun userRepositoryImpl() : UserRepository = UserRepositoryImpl(userDao, profilePhotoDao, storageHelper)
////
////
////
////    @Provides
////    @Singleton
////    fun groupRepositoryFunctionProviderDelegate() : GroupRepositoryFunctionProviderDelegate = GroupRepositoryFunctionProviderDelegateImpl(groupRepositoryImpl,userRepositoryImpl() as UserRepositoryImpl)
////
////    @Provides
////    @Singleton
////    fun groupRepositoryOfGroupProfile() : GroupRepository.GroupProfile = groupRepositoryImpl
////
////
////    @Provides
////    @Singleton
////    fun signUpUseCase() : SignUpUseCase = userUseCaseImpl
////
////
////    @Provides
////    @Singleton
////    fun loginUseCase() : LoginUseCase = userUseCaseImpl
//
//}