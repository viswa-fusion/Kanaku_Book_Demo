package com.example.data.repositoryImpl

import android.graphics.Bitmap
import android.util.Log
import com.example.domain.repository.GroupRepository
import com.example.kanakunote.data_layer.crossreference.GroupMemberCrossRef
import com.example.data.dao.GroupDao
import com.example.data.util.StorageHelper
import com.example.data.util.toGroupData
import com.example.data.util.toGroupEntity
import com.example.domain.model.GroupData
import com.example.domain.model.GroupEntry
import com.example.domain.repository.response.DataLayerErrorCode
import com.example.domain.repository.response.DataLayerResponse
import com.example.kanakunote.data_layer.dao.ProfilePhotoDao
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File

class RepositoryImpl(
    private val groupDao: GroupDao,
    private val profilePhotoDao: ProfilePhotoDao,
    private val storageHelper: StorageHelper
) : GroupRepository.Info, GroupRepository.Profile {

    private val groupProfileImageFileDir: File
        get() {
            val fileDir =
                storageHelper.getInternalStoragePath(StorageHelper.GROUP_PROFILE_PHOTO_DIRECTORY)
            if (!fileDir.exists()) fileDir.mkdirs()
            return fileDir
        }

    override suspend fun insertGroupEntry(group: GroupEntry): DataLayerResponse<Boolean> {
        return try {
            val groupEntity = group.toGroupEntity()
            val groupId = groupDao.insertGroup(groupEntity)
            val crossRef = group.members.map { member -> GroupMemberCrossRef(groupId, member) }

            coroutineScope {
                val crossRefResult = async { groupDao.insertGroupMembers(crossRef) }
                val imageUploadResult = async {
                    group.profilePicture?.let { profilePicture ->
                        saveProfileImage(groupId, profilePicture)
                    }
                }
                awaitAll(crossRefResult, imageUploadResult)
            }
            DataLayerResponse.Success(true)
        } catch (e: Exception) {
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }


    override suspend fun retrieveUserGroupsByUserId(userId: Long): DataLayerResponse<List<GroupData>> {
        val listOfGroupEntity = groupDao.getGroupsWithMembersByUserId(userId)
        val listOfGroup = listOfGroupEntity.map { it.toGroupData() }
        return DataLayerResponse.Success(listOfGroup)
    }

    override suspend fun saveProfileImage(
        groupId: Long,
        image: Bitmap?
    ): DataLayerResponse<Boolean> {
        val file = File(groupProfileImageFileDir, "$groupId${StorageHelper.IMAGE_TYPE_JPG}")
        return profilePhotoDao.saveImage(image, file)
    }
    override suspend fun getProfilePhoto(groupId: Long): DataLayerResponse<Bitmap> {
        val file = File(groupProfileImageFileDir, "$groupId${StorageHelper.IMAGE_TYPE_JPG}")
        Log.i("dataTest","data : $file")
        return profilePhotoDao.getProfilePhoto(file.absolutePath)
    }
}