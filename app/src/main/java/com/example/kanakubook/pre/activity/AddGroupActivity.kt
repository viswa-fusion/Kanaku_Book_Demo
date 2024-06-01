package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.databinding.AddGroupScreenActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.viewmodel.GroupViewModel
import com.example.kanakubook.util.ImageConversionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.net.URL

class AddGroupActivity: AppCompatActivity() {

    private lateinit var binding: AddGroupScreenActivityBinding
    private val viewModel : GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private var isBottomSheetOpen = false
    private var profileUri: Uri? = null
    private val PROFILE_URI_KEY = "group profile"
    private lateinit var preferenceHelper : PreferenceHelper

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    profileUri = selectedImageUri
                    binding.imageview.setImageURI(selectedImageUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (profileUri != null) {
            outState.putString(PROFILE_URI_KEY, profileUri.toString())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val profileUri = savedInstanceState.getString(PROFILE_URI_KEY)
        if (profileUri != null) {
            val parseUri = Uri.parse(profileUri)
            this.profileUri = parseUri
            binding.imageview.setImageURI(this.profileUri)
        }
    }

    private fun setListener(){
        binding.imageview.setOnClickListener {
            if(!isBottomSheetOpen){
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityResultForProfilePhoto.launch(intent)
                isBottomSheetOpen = true
            }
        }

        binding.button2.setOnClickListener {
            val groupName = binding.groupname.text.toString()
            val ids = binding.members.text.toString().split(",").map {
                it.toLong()
            }.toMutableList()
            val logUserId = loggedInUserId()
            ids.add(logUserId)
            viewModel.createGroup(this, logUserId, groupName, profileUri, ids)
        }
    }

    private fun setObserver(){
        viewModel.groupCreateResponse.observe(this){
            when(it){
                is PresentationLayerResponse.Success -> {
                    Toast.makeText(this,"success", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this,"fail", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    private fun loggedInUserId(): Long {
        return if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
             preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
        } else {
            val intent = Intent(this, AppEntryPoint::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            -1
        }
    }

    private fun initialSetUp(){
        binding = AddGroupScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceHelper = PreferenceHelper(this)
    }
}