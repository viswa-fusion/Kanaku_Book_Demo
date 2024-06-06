package com.example.domain.usecase


import android.graphics.Bitmap
import com.example.domain.helper.CryptoHelper
import com.example.domain.model.UserEntryData
import com.example.domain.model.UserProfileSummary
import com.example.domain.repository.GroupRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.ImageDirectoryType


class UserUseCaseImpl(
    private val userInfoRepo: UserRepository.Info,
    private val userAuthenticationRepo: UserRepository.Authentication,
    private val groupRepo: GroupRepository.Profile,
    private val userProfile: UserRepository.UserProfile
    ) : SignUpUseCase,
    LoginUseCase,
    ProfilePictureUseCase,
    UserUseCase.FriendsUseCase,
    UserUseCase.CommonUserUseCase{

    override suspend fun addUser(
        name: String,
        phone: Long,
        password: String,
        repeatPassword: String
    ): PresentationLayerResponse<Long> {

        val userEntryData = UserEntryData(
            name,
            phone,
            password
        )
        return when (val result = userInfoRepo.insertUser(userEntryData, password)) {
            is DataLayerResponse.Success -> PresentationLayerResponse.Success(CryptoHelper.encrypt(result.data))
            is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
        }
    }

    override suspend fun authenticateUser(
        phone: Long,
        password: String
    ): PresentationLayerResponse<UserProfileSummary> {
        return when (val result = userAuthenticationRepo.authenticateUser(phone)) {
            is DataLayerResponse.Success -> {
                if (result.data.second == password) {
                    val resultEntity = result.data.first
                    val data = resultEntity.copy(userId = CryptoHelper.encrypt(resultEntity.userId))
                    PresentationLayerResponse.Success(data)
                } else PresentationLayerResponse.Error("wrong password")
            }

            is DataLayerResponse.Error -> PresentationLayerResponse.Error("user not found")
        }
    }

    override suspend fun addProfileImage(
        imageDirectoryType: ImageDirectoryType,
        image: Bitmap
    ): PresentationLayerResponse<Boolean> {
        return when (imageDirectoryType) {
            is ImageDirectoryType.User -> {
                when (val result = userProfile.saveUserProfileImage(imageDirectoryType.userId, image)) {
                    is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
                    is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
                }
            }

            is ImageDirectoryType.Group -> {
                when (val result = groupRepo.saveProfileImage(imageDirectoryType.groupId, image)) {
                    is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
                    is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
                }
            }
        }
    }

    override suspend fun getProfileImage(
        imageDirectoryType: ImageDirectoryType
    ): PresentationLayerResponse<Bitmap?> {
        return when (imageDirectoryType) {
            is ImageDirectoryType.User -> {
                val decryptId = CryptoHelper.decrypt(imageDirectoryType.userId)
                when (val result = userProfile.getUserProfilePhoto(decryptId)) {
                    is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
                    is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
                }
            }

            is ImageDirectoryType.Group -> {
                val decryptId = CryptoHelper.decrypt(imageDirectoryType.groupId)
                when (val result = groupRepo.getProfilePhoto(decryptId)) {
                    is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
                    is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
                }
            }
        }
    }

    override suspend fun addFriend(
        userId: Long,
        friendPhone: Long
    ): PresentationLayerResponse<Boolean> {
        return when (val friendUserId = userInfoRepo.getUserIdByPhone(friendPhone)) {
            is DataLayerResponse.Success -> {
                when (val result = userInfoRepo.addFriend(CryptoHelper.decrypt(userId), friendUserId.data)) {
                    is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
                    is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
                }
            }

            is DataLayerResponse.Error -> PresentationLayerResponse.Error(friendUserId.errorCode.toString())
        }
    }

    override suspend fun getMyFriends(userId: Long): PresentationLayerResponse<List<UserProfileSummary>> {
        val id = CryptoHelper.decrypt(userId)
        return when(val listOfUsers = userInfoRepo.getFriendsOfUser(id)){
            is DataLayerResponse.Success -> {
                val decryptedList =listOfUsers.data.map{
                    it.copy(userId = CryptoHelper.encrypt(it.userId)).apply { connectionId = CryptoHelper.encrypt(it.connectionId!!) }
                }
                PresentationLayerResponse.Success(decryptedList)
            }

            is DataLayerResponse.Error -> PresentationLayerResponse.Error(listOfUsers.errorCode.toString())
        }
    }

    override suspend fun getUserById(userId: Long): PresentationLayerResponse<UserProfileSummary> {
        val id = CryptoHelper.decrypt(userId)
        return when(val user = userInfoRepo.getUser(id)){
            is DataLayerResponse.Success -> {
                val encryptData = user.data.copy(userId = CryptoHelper.decrypt(user.data.userId))
                PresentationLayerResponse.Success(encryptData)
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error(user.errorCode.toString())
        }
    }

    override suspend fun getAllKanakuBookUsers(userId: Long): PresentationLayerResponse<List<UserProfileSummary>> {
        return when(val list = userInfoRepo.getAllUsersExceptMyFriends(CryptoHelper.decrypt(userId))){
            is DataLayerResponse.Success -> {
                val encryptList = list.data.map {
                    it.copy(
                        userId = CryptoHelper.encrypt(it.userId)
                    )
                }
                PresentationLayerResponse.Success(encryptList)
            }

            is DataLayerResponse.Error -> {
                PresentationLayerResponse.Error(list.errorCode.toString())
            }
        }

    }
}