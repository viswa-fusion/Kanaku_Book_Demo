package com.example.data.repositoryImpl

import android.graphics.Bitmap
import android.util.Log
import com.example.kanakunote.data_layer.dao.ProfilePhotoDao
import com.example.data.dao.UserDao
import com.example.data.util.StorageHelper
import com.example.data.util.toUserEntity
import com.example.data.util.toUserProfileSummery
import com.example.domain.model.UserEntryData
import com.example.domain.model.UserProfileSummary

import com.example.domain.repository.UserRepository
import com.example.domain.repository.response.DataLayerErrorCode
import com.example.domain.repository.response.DataLayerResponse
import com.example.kanakunote.data_layer.crossreference.FriendsConnectionCrossRef
import java.io.File

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val profilePhotoDao: ProfilePhotoDao,
    private val storageHelper: StorageHelper
) : UserRepository.Info, UserRepository.Authentication, UserRepository.UserProfile {
    private val userProfileImageFileDir: File
        get() {
            val fileDir =
                storageHelper.getInternalStoragePath(StorageHelper.USER_PROFILE_PHOTO_DIRECTORY)
            if (!fileDir.exists()) fileDir.mkdirs()
            return fileDir
        }

    override suspend fun insertUser(userEntryData: UserEntryData, password: String): DataLayerResponse<Long> {
        val userEntity = userEntryData.toUserEntity(password)
        return userDao.insertUser(userEntity).run {
            if (this > 0) DataLayerResponse.Success(this)
            else DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    override suspend fun updateUser(userEntryData: UserEntryData): DataLayerResponse<Boolean> {
        val userEntity = userEntryData.toUserEntity()
        return userDao.updateUser(userEntity).run {
            if (this > 0) DataLayerResponse.Success(true)
            else DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    override suspend fun getUser(userId: Long): DataLayerResponse<UserProfileSummary> {
        val user = userDao.getUserById(userId)
        return if (user != null) {
            val profilePath = File(
                userProfileImageFileDir,
                "${user.userId}${StorageHelper.IMAGE_TYPE_JPG}"
            ).absolutePath
            DataLayerResponse.Success(user.toUserProfileSummery())
        } else {
            DataLayerResponse.Error(DataLayerErrorCode.NOT_FOUND)
        }

    }

    override suspend fun deleteUser(userEntryData: UserEntryData): DataLayerResponse<Boolean> {
        val userEntity = userEntryData.toUserEntity()
        return userDao.deleteUser(userEntity).run {
            if (this > 0) DataLayerResponse.Success(true)
            else DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    override suspend fun getUserIdByPhone(phone: Long): DataLayerResponse<Long> {
        return userDao.getUserIdByPhone(phone).run {
            if (this > 0) DataLayerResponse.Success(this)
            else DataLayerResponse.Error(DataLayerErrorCode.NOT_FOUND)
        }
    }

    override suspend fun addFriend(userId: Long, friendId: Long): DataLayerResponse<Boolean> {
        val friendsConnectionCrossRef = FriendsConnectionCrossRef(userId, friendId)
        Log.i("testData", "data : $friendsConnectionCrossRef")
        return userDao.insertFriendsConnection(friendsConnectionCrossRef).run {
            DataLayerResponse.Success(true)
        }
    }

    override suspend fun getFriendsOfUser(userId: Long): DataLayerResponse<List<UserProfileSummary>> {

        val userProfileSummaryList = mutableListOf<UserProfileSummary>()

        userDao.getFriendsOfUser(userId).forEach {
            val profilePath = File(
                userProfileImageFileDir,
                "${it.userId}${StorageHelper.IMAGE_TYPE_JPG}"
            ).absolutePath
            userProfileSummaryList.add(it.toUserProfileSummery())
        }
        return DataLayerResponse.Success(userProfileSummaryList)
    }

    override suspend fun getUserProfileSummeryByUserId(
        userId: Long
    ): DataLayerResponse<UserProfileSummary> {
        return userDao.getUserById(userId).run {
            if (this != null) {
                val profilePath = File(
                    userProfileImageFileDir,
                    "$userId${StorageHelper.IMAGE_TYPE_JPG}"
                ).absolutePath
                DataLayerResponse.Success(this.toUserProfileSummery())
            } else DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    override suspend fun getAllUsers(): DataLayerResponse<List<UserProfileSummary>> {
        return userDao.getAllUsers().run {
            val list = mutableListOf<UserProfileSummary>()
            this.forEach {
                val profilePath = File(
                    userProfileImageFileDir,
                    "${it.userId}${StorageHelper.IMAGE_TYPE_JPG}"
                ).absolutePath
                list.add(it.toUserProfileSummery())
            }
            if (this.isNotEmpty()) DataLayerResponse.Success(list)
            else DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    override suspend fun authenticateUser(phone: Long): DataLayerResponse<Pair<UserProfileSummary, String>> {
        val userEntity = userDao.getUserByCredentials(phone)
        return if (userEntity != null) {
            val profilePath = File(
                userProfileImageFileDir,
                "${userEntity.userId}${StorageHelper.IMAGE_TYPE_JPG}"
            ).absolutePath
            val user = userEntity.toUserProfileSummery()
            DataLayerResponse.Success(Pair(user, userEntity.password))
        } else DataLayerResponse.Error(DataLayerErrorCode.NOT_FOUND)
    }

    override suspend fun saveUserProfileImage(
        userId: Long,
        image: Bitmap
    ): DataLayerResponse<Boolean> {
        val file = File(userProfileImageFileDir, "$userId${StorageHelper.IMAGE_TYPE_JPG}")
        return profilePhotoDao.saveImage(image, file)
    }

    override suspend fun getUserProfilePhoto(userId: Long): DataLayerResponse<Bitmap> {
        val file = File(userProfileImageFileDir, "$userId.JPG")
        return profilePhotoDao.getProfilePhoto(file.absolutePath)
    }
}