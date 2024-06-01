package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.databinding.AddFriendScreenActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFriendActivity : AppCompatActivity() {

    private lateinit var binding: AddFriendScreenActivityBinding
    private lateinit var preferenceHelper: PreferenceHelper
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddFriendScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setObserver()

        preferenceHelper = PreferenceHelper(this)
        binding.button.setOnClickListener {
            if(preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
                val userId = preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
//                val friendPhoneNumber = binding.textView.text.toString().toLong()
                lifecycleScope.launch(Dispatchers.IO) {
//                    viewModel.addFriend(userId, friendPhoneNumber)
                    for (i in 10..100) {
                        viewModel.addFriend(userId, 9487212880 + i)
                    }
                }
            }else{
                val intent = Intent(this,AppEntryPoint::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    private fun setObserver(){
        viewModel.addFriend.observe(this){
            when(it){
                is PresentationLayerResponse.Success -> {
                    Toast.makeText(this,"success",Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
//                    finish()
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this,"fail",Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CANCELED)
//                    finish()
                }
            }
        }
    }
}