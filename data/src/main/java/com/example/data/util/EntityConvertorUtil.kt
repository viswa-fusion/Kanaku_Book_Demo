package com.example.data.util

import com.example.domain.model.GroupSummery
import com.example.domain.model.UserData
import com.example.domain.model.UserProfileData
import com.example.domain.model.UserProfileSummary
import com.example.data.entity.GroupEntity
import com.example.data.relation.GroupWithMembers
import com.example.data.entity.UserEntity
import com.example.domain.model.Group
import com.example.domain.model.GroupData


fun UserData.toUserEntity(password: String = "fakePassword"): UserEntity {
    return UserEntity(
        this.name,
        this.phone,
        password
    )
}

fun UserEntity.toUserProfileSummery(profilePhotoFilePath: String): UserProfileSummary {
    return UserProfileSummary(
        this.userId,
        this.name,
        this.phone,
        profilePhotoFilePath
    )
}

fun UserEntity.toUserProfileData(
    profilePhotoFilePath: String,
    amountToGet: Double,
    amountToGive: Double
): UserProfileData {
    return UserProfileData(
        this.userId,
        this.name,
        this.phone,
        profilePhotoFilePath,
        amountToGet,
        amountToGive
    )
}

fun GroupWithMembers.toGroupSummery(profilePhotoFilePath: String): GroupSummery {
    return GroupSummery(
        this.group.groupName,
        profilePhotoFilePath,
        this.group.createdBy
    )
}


fun Group.toGroupEntity(): GroupEntity {
    return GroupEntity(
        this.name,
        this.createdBy
    )
}

fun GroupData.toGroupEntity(): GroupEntity {
    return GroupEntity(
        this.name,
        this.createdBy
    )
}

fun GroupWithMembers.toGroup(profilePathDirectory: String): Group {
    return Group(
        this.group.groupId,
        this.group.groupName,
        profilePathDirectory + "/${this.group.groupId}.JPG",
        this.group.createdBy,
        this.members.map {
            it.toUserProfileSummery(profilePathDirectory + "/${it.userId}.JPG")
        }
    )
}
