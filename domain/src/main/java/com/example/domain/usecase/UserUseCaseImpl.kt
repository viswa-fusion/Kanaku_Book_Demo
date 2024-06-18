package com.example.domain.usecase


import android.graphics.Bitmap
import com.example.domain.Converters.ActivityType
import com.example.domain.helper.CryptoHelper
import com.example.domain.helper.DateTimeHelper
import com.example.domain.model.ActivityModel
import com.example.domain.model.ActivityModelEntry
import com.example.domain.model.UserEntryData
import com.example.domain.model.UserProfileData
import com.example.domain.model.UserProfileSummary
import com.example.domain.repository.GroupRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.response.ActivityRepository
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.ImageDirectoryType


class UserUseCaseImpl(
    private val userInfoRepo: UserRepository.Info,
    private val userAuthenticationRepo: UserRepository.Authentication,
    private val groupRepo: GroupRepository.Profile,
    private val userProfile: UserRepository.UserProfile,
    private val activityRepo: ActivityRepository,
    ) : SignUpUseCase,
    LoginUseCase,
    ProfilePictureUseCase,
    UserUseCase.FriendsUseCase,
    UserUseCase.CommonUserUseCase,
    ActivityUseCase{

    override suspend fun addUser(
        name: String,
        phone: Long,
        dob:String?,
        password: String,
        repeatPassword: String
    ): PresentationLayerResponse<Long> {

        val userEntryData = UserEntryData(
            name,
            phone,
            password,
            dob?.let {DateTimeHelper.dateStringToMillis(dob)}
        )
        return when(val checkValid = userInfoRepo.checkPhoneNumberExist(phone)){
            is DataLayerResponse.Success -> {
                 if (checkValid.data){
                    PresentationLayerResponse.Error("user exist")
                }else{
                    when (val result = userInfoRepo.insertUser(userEntryData, password)) {
                        is DataLayerResponse.Success -> PresentationLayerResponse.Success(CryptoHelper.encrypt(result.data))
                        is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
                    }
                }
            }
            is DataLayerResponse.Error -> PresentationLayerResponse.Error(checkValid.errorCode.toString())
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

    override suspend fun loggedUserByUserId(userId: Long): PresentationLayerResponse<UserProfileData> {
       return when(val result =  userAuthenticationRepo.loggedUserByUserId(CryptoHelper.decrypt(userId))){
           is DataLayerResponse.Success -> {
               val encryptData = result.data.copy(
                   userId = CryptoHelper.encrypt(result.data.userId)
               )
               PresentationLayerResponse.Success(encryptData)
           }

           is DataLayerResponse.Error -> {
               PresentationLayerResponse.Error(result.errorCode.toString())
           }
       }
    }

    override suspend fun addProfileImage(
        imageDirectoryType: ImageDirectoryType,
        image: Bitmap
    ): PresentationLayerResponse<Boolean> {
        return when (imageDirectoryType) {
            is ImageDirectoryType.User -> {
                when (val result = userProfile.saveUserProfileImage(CryptoHelper.decrypt(imageDirectoryType.userId), image)) {
                    is DataLayerResponse.Success -> PresentationLayerResponse.Success(result.data)
                    is DataLayerResponse.Error -> PresentationLayerResponse.Error(result.errorCode.toString())
                }
            }

            is ImageDirectoryType.Group -> {
                when (val result = groupRepo.saveProfileImage(CryptoHelper.decrypt(imageDirectoryType.groupId), image)) {
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
                when (val result = userInfoRepo.addFriend(CryptoHelper.decrypt(userId), friendUserId.data, DateTimeHelper.getCurrentTime())) {
                    is DataLayerResponse.Success -> {
                        val activity = ActivityModelEntry(
                            -1L,
                            CryptoHelper.decrypt(userId),
                            ActivityType.ADD_FRIEND,
                            DateTimeHelper.getCurrentTime(),
                            null,
                            friendId = friendUserId.data,
                            connectionId = result.data
                        )
                        when(val activityResult = activityRepo.insertActivity(activity)){
                            is DataLayerResponse.Success -> PresentationLayerResponse.Success(true)
                            is DataLayerResponse.Error -> PresentationLayerResponse.Error(activityResult.errorCode.toString())
                        }
                    }
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
                val decryptedList = listOfUsers.data.map{
                    it.copy(userId = CryptoHelper.encrypt(it.userId)).apply {
                        connectionId = CryptoHelper.encrypt(it.connectionId!!)
                        pay = it.pay
                        get = it.get
                    }
                }
                PresentationLayerResponse.Success(decryptedList)
            }

            is DataLayerResponse.Error -> PresentationLayerResponse.Error(listOfUsers.errorCode.toString())

        }
    }

    override suspend fun updateUser(
        userId: Long,
        userName: String?,
        dob: String?,
        profile: Bitmap?
    ): PresentationLayerResponse<Boolean> {
        return try{
            val decryptUserId = CryptoHelper.decrypt(userId)
            profile?.let { userProfile.saveUserProfileImage(decryptUserId, profile) }
            userName?.let {
                val dateOfBirth = dob?.let { it1 -> DateTimeHelper.dateStringToMillis(it1) }
                userInfoRepo.updateUser(decryptUserId, userName, dateOfBirth)
            }
            PresentationLayerResponse.Success(true)
        }catch (e:Exception){
            PresentationLayerResponse.Error("edit profile failed")
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

    override suspend fun getAllMyActivity(userId: Long): PresentationLayerResponse<List<ActivityModel>> {
       return when(val result =  activityRepo.getAllMyActivity(CryptoHelper.decrypt(userId))){
           is DataLayerResponse.Success -> {
               val encryptData = result.data.map {
                   it.copy(
                       activityId = CryptoHelper.encrypt(it.activityId),
                       user = it.user.copy(
                           userId = CryptoHelper.encrypt(it.user.userId)
                       ),
                       friend = it.friend?.let {_-> it.friend.copy(
                           userId = CryptoHelper.encrypt(it.friend.userId)
                       ) },
                       group = it.group?.let {_-> it.group.copy(
                          id = CryptoHelper.encrypt(it.group.id)
                       ) },
                       expense = it.expense?.let {_-> it.expense.copy(
                           expenseId = CryptoHelper.encrypt(it.expense.expenseId).dec(),
                           listOfSplits = it.expense.listOfSplits.map {list ->
                               list.copy(
                                   splitUserId = CryptoHelper.encrypt(list.splitUserId)
                               )
                           }
                       ) },
                       connectionId = it.connectionId?.let {_-> CryptoHelper.encrypt(it.connectionId)}
                   )
               }
               PresentationLayerResponse.Success(encryptData)
           }

           is DataLayerResponse.Error -> {
                PresentationLayerResponse.Error(result.errorCode.toString())
           }
       }
    }
}