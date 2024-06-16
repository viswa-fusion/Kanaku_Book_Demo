package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.crossreference.ExpenseCrossRef
import com.example.data.dao.ExpenseDao
import com.example.data.crossreference.FriendsConnectionCrossRef
import com.example.data.crossreference.GroupMemberCrossRef
import com.example.data.dao.GroupDao
import com.example.data.dao.SplitDao
import com.example.kanakunote.data_layer.dao.ProfilePhotoDao
import com.example.data.dao.UserDao
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.GroupEntity
import com.example.data.entity.SplitEntity
import com.example.data.entity.UserEntity
import com.example.domain.Converters.Converters
import com.example.data.crossreference.SplitExpenseCrossRef
import com.example.data.dao.ActivityDao
import com.example.data.entity.ActivityEntity

@Database(
    entities = [
        UserEntity::class,
        FriendsConnectionCrossRef::class,
        GroupEntity::class,
        GroupMemberCrossRef::class,
        ExpenseEntity::class,
        SplitEntity::class,
        ExpenseCrossRef::class,
        SplitExpenseCrossRef::class,
        ActivityEntity::class
    ], version = 3
)
@TypeConverters(Converters::class)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun getMyUserDao(): UserDao
    abstract fun getMyGroupDao(): GroupDao
    abstract fun getMyProfilePhotoDao(): ProfilePhotoDao
    abstract fun getMyExpenseDao(): ExpenseDao
    abstract fun getMySplitDao(): SplitDao
    abstract fun getMyActivityDao(): ActivityDao


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
