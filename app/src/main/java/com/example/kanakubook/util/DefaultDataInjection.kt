package com.example.kanakubook.util


import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.data.database.ApplicationDatabase
import com.example.data.repositoryImpl.UserRepositoryImpl
import com.example.domain.helper.CryptoHelper
import com.example.domain.helper.DateTimeHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.StorageHelperImpl
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.presentation.viewmodel.SignUpViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.github.javafaker.Faker
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DefaultDataInjection(val context: Context) {

    private val ob = context as KanakuBookApplication
    private val db = ApplicationDatabase.getDatabase(context)
    private val viewModel = SignUpViewModel(ob.signUpUseCase)
    val profilePictureUseCase = db.getMyProfilePhotoDao()
    private val photoViewModel =
        FriendsViewModel(ob.friendsUseCase, ob.profilePictureUseCase, ob.friendsExpenseUseCase)
    private val repo =
        UserRepositoryImpl(db.getMyUserDao(), db.getMyProfilePhotoDao(), StorageHelperImpl(context))

    private data class DefaultData(
        val name: String,
        val phone: Long,
        val password: String,
        val repeatPassword: String
    )

    fun addDefault() {
        setObserver()
        val faker = Faker()
        val dataList = mutableListOf<DefaultData>()
        val initialPhoneNumber = 9487212887L
        val password = "Test@123"
        repeat(500) { index ->
            val name = faker.name().fullName()
            val phone = initialPhoneNumber + index
            val defaultData = DefaultData(name, phone, password, password)
            dataList.add(defaultData)
        }

        dataList.forEach {
            viewModel.signUp(it.name, it.phone, "19/5/2001", it.password, it.repeatPassword)
            Log.i("initialData123", "data : ${it.phone}")
        }
    }

    private fun setObserver() {
        val listOfProfile = listOf(
            R.raw.dummy_img_1,
            R.raw.dummy_img_2,
            R.raw.dummy_img_3,
            R.raw.dummy_img_4,
            R.raw.dummy_img_5,
            R.raw.dummy_img_6,
            R.raw.dummy_img_8,
            R.raw.dummy_img_9,
            R.raw.dummy_img_10,
            R.raw.dummy_img_11,
            R.raw.dummy_img_12,
            R.raw.dummy_img_13,
            R.raw.dummy_img_14,
            R.raw.dummy_img_15,
            R.raw.dummy_img_16,
            R.raw.dummy_img_17,
        )
        var userid: Long? = null
        var count = 1
        viewModel.userId.observeForever {
            if (it is PresentationLayerResponse.Success) {
                if (count < 100) {
                    if (userid == null) {
                        userid = CryptoHelper.decrypt(it.data)
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.addFriend(
                                userid!!,
                                CryptoHelper.decrypt(it.data),
                                DateTimeHelper.getCurrentTime()
                            )
                        }
                    }
                    count++
                }
                val photoId = listOfProfile[Random.nextInt(0, 16)]
                val photo = BitmapFactory.decodeResource(context.resources, photoId)
                photoViewModel.addProfile(it.data, photo)
            }
        }
    }

    suspend fun copyDefaultProfileImages(context: Context) = withContext(Dispatchers.IO) {
        val profileImageFolder = File(context.filesDir, "userProfilePhoto")
        profileImageFolder.mkdirs()

        val groupProfileImageFolder = File(context.filesDir, "GroupProfilePhoto")
        groupProfileImageFolder.mkdirs()

        launch {
            try {
                val defaultImageNames = context.assets.list("userProfilePhoto") ?: emptyArray()
                for (imageName in defaultImageNames) {
                    val inputStream = context.assets.open("userProfilePhoto/$imageName")
                    val outputStream = FileOutputStream(File(profileImageFolder, imageName))
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        launch {
            try {
                val defaultImageNames = context.assets.list("GroupProfilePhoto") ?: emptyArray()
                for (imageName in defaultImageNames) {
                    val inputStream = context.assets.open("GroupProfilePhoto/$imageName")
                    val outputStream = FileOutputStream(File(groupProfileImageFolder, imageName))
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


    }

}