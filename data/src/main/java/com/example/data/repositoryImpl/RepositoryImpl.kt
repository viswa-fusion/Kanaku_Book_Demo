package com.example.data.repositoryImpl

import android.graphics.Bitmap
import android.util.Log
import com.example.domain.repository.GroupRepository
import com.example.data.crossreference.GroupMemberCrossRef
import com.example.data.dao.GroupDao
import com.example.data.util.StorageHelper
import com.example.data.util.toCommonGroupWithAmountData
import com.example.data.util.toGroupData
import com.example.data.util.toGroupEntity
import com.example.domain.model.CommonGroupWIthAmountData
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

    override suspend fun insertGroupEntry(group: GroupEntry): DataLayerResponse<Long> {
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
            DataLayerResponse.Success(groupId)
        } catch (e: Exception) {
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }


    override suspend fun retrieveUserGroupsByUserId(userId: Long): DataLayerResponse<List<GroupData>> {
        val listOfGroupEntity = groupDao.getGroupsWithMembersAndBalances(userId)
        val listOfGroup = listOfGroupEntity.map { it.toGroupData() }

        listOfGroupEntity.forEach {
            Log.i("QueryData","data: $it")
        }

        return DataLayerResponse.Success(listOfGroup)
    }

    override suspend fun updateGroupActiveTime(groupId: Long, time: Long) {
        groupDao.updateGroupActiveTime(groupId, time)
    }
    override suspend fun addMembers(
        groupId: Long,
        membersList: List<Long>
    ): DataLayerResponse<Boolean> {
        return try{
            val crossRef = membersList.map { memberId -> GroupMemberCrossRef(groupId, memberId) }
            groupDao.insertGroupMembers(crossRef)
            DataLayerResponse.Success(true)
        }catch (e:Exception){
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }

    }

    override suspend fun getGroupByGroupId(groupId: Long): DataLayerResponse<GroupData> {
        return try{
            DataLayerResponse.Success(groupDao.getGroupByGroupId(groupId).toGroupData())
        }catch (e:Exception){
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
    }

    override suspend fun getCommonGroupsWithCalculatedBalance(
        userId: Long,
        friendId: Long
    ): DataLayerResponse<List<CommonGroupWIthAmountData>> {
        return try{
            val result = groupDao.getCommonGroupsWithCalculatedBalance(userId, friendId).map {
                it.toCommonGroupWithAmountData()
            }
            DataLayerResponse.Success(result)
        }catch (e: Exception){
            DataLayerResponse.Error(DataLayerErrorCode.OPERATION_FAILED)
        }
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