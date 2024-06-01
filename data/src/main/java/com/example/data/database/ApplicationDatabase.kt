package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kanakunote.data_layer.crossreference.FriendsConnectionCrossRef
import com.example.kanakunote.data_layer.crossreference.GroupMemberCrossRef
import com.example.data.dao.GroupDao
import com.example.kanakunote.data_layer.dao.ProfilePhotoDao
import com.example.data.dao.UserDao
import com.example.data.entity.GroupEntity
import com.example.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FriendsConnectionCrossRef::class,
        GroupEntity::class,
        GroupMemberCrossRef::class
    ], version = 1
)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun getMyUserDao(): UserDao
    abstract fun getMyGroupDao(): GroupDao
    abstract fun getMyProfilePhotoDao(): ProfilePhotoDao

    companion object {
        private var INSTANCE: ApplicationDatabase? = null
        fun getDatabase(context: Context): ApplicationDatabase {
            return if (INSTANCE == null) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ApplicationDatabase::class.java,
                    "kanaku_note_database"
                )
                builder.fallbackToDestructiveMigration()
                builder.build()
            } else {
                INSTANCE!!
            }
        }
    }
}
