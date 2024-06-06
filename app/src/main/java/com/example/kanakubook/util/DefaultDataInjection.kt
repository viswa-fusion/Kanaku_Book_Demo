package com.example.kanakubook.util


import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.data.database.ApplicationDatabase
import com.example.data.repositoryImpl.UserRepositoryImpl
import com.example.data.util.StorageHelper
import com.example.domain.helper.CryptoHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.StorageHelperImpl
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.SignUpViewModel
import com.example.kanakubook.pre.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.github.javafaker.Faker

class DefaultDataInjection(val context: Context) {

    private val ob = context as KanakuBookApplication
    private val db = ApplicationDatabase.getDatabase(context)
    private val viewModel = SignUpViewModel(ob.signUpUseCase)
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
        repeat(100) { index ->
            val name = faker.name().fullName()
            val phone = initialPhoneNumber + index
            val defaultData = DefaultData(name, phone, password, password)
            dataList.add(defaultData)
        }

        dataList.forEach {
            viewModel.signUp(it.name, it.phone, it.password, it.repeatPassword)
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
            R.raw.dummy_img_6
        )
        var userid: Long? = null
        viewModel.userId.observeForever {
            if (it is PresentationLayerResponse.Success) {
                if (userid == null) {
                    userid = CryptoHelper.decrypt(it.data)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        repo.addFriend(userid!!, CryptoHelper.decrypt(it.data))
                    }
                }
                val photoId = listOfProfile[Random.nextInt(0, 5)]
                val photo = BitmapFactory.decodeResource(context.resources, photoId)
                photoViewModel.addProfile(CryptoHelper.decrypt(it.data), photo)
                Log.i("initialData321", "data : ${it.data}")
            }
        }
    }
}