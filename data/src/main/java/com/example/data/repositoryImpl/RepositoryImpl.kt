package com.example.data.repositoryImpl

import android.graphics.Bitmap
import android.util.Log
import com.example.domain.repository.GroupRepository
import com.example.kanakunote.data_layer.crossreference.GroupMemberCrossRef
import com.example.data.dao.GroupDao
import com.example.data.util.StorageHelper
import com.example.data.util.toGroup
import com.example.data.util.toGroupEntity
import com.example.domain.model.Group
import com.example.domain.model.GroupData
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

    override suspend fun insertGroup(group: GroupData): DataLayerResponse<Boolean> {
        val groupEntity = group.toGroupEntity()
        val groupId = groupDao.insertGroup(groupEntity)
        val crossRef = mutableListOf<GroupMemberCrossRef>()
        Log.i("testCase","id: $groupId")

        group.members.forEach {
            crossRef.add(GroupMemberCrossRef(groupId, it))
        }
        coroutineScope {
            val crossRefResult = async { groupDao.insertGroupMembers(crossRef) }
            val imageUploadResult = async {
                if(group.profilePicture != null){
                    saveProfileImage(groupId, group.profilePicture)
                }
            }
            awaitAll(crossRefResult,imageUploadResult)
        }
        return DataLayerResponse.Success(true)
    }

    override suspend fun retrieveUserGroupsByUserId(userId: Long): DataLayerResponse<List<Group>> {
        val listOfGroupEntity = groupDao.getGroupsWithMembersByUserId(userId)
        val listOfGroup = listOfGroupEntity.map {
            Log.i("testData","raw dat: ${it.members}")
            val file = File(groupProfileImageFileDir, "${it.group.groupId}groupId${StorageHelper.IMAGE_TYPE_JPG}").absolutePath
            it.toGroup(groupProfileImageFileDir.absolutePath) }
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