package com.example.kanakubook.pre.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.SignUpScreenFragmentBinding
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.SignUpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class SignUpScreenFragment : Fragment(R.layout.sign_up_screen_fragment) {

    private lateinit var binding: SignUpScreenFragmentBinding

    private val viewModel: SignUpViewModel by viewModels { SignUpViewModel.FACTORY }
    private val vm: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var profileUri: Uri? = null
    private val PROFILE_URI_KEY = "profile_uri"
    private var isBottomSheetOpen = false
    private val TAG = "Tag"

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    profileUri = selectedImageUri
                    binding.imageProfile.setImageURI(selectedImageUri)
                }
            }
            isBottomSheetOpen = false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SignUpScreenFragmentBinding.bind(view)
        setObserver()




        binding.imageProfile.setOnClickListener {
            selectImageFromGallery()
        }
        binding.buttonSignUp.setOnClickListener {
            for (i in 1..100) {
                viewModel.signUp(
                    "viswa$i",
                    9487212880 + i,
                    "Test@123",
                    "Test@123"
                )
            }
//            saveUserData(
//                binding.editTextName.text.toString(),
//                binding.editTextPhoneNumber.text.toString().toLong(),
//                binding.editTextPassword.text.toString(),
//                binding.editTextRepeatPassword.text.toString(),
//            )
        }

        binding.goBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        val profileUri = savedInstanceState?.getString(PROFILE_URI_KEY)
        if (profileUri != null) {
            val parseUri = Uri.parse(profileUri)
            this.profileUri = parseUri
            binding.imageProfile.setImageURI(this.profileUri)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (profileUri != null) {
            outState.putString(PROFILE_URI_KEY, profileUri.toString())
        }
    }

    private fun selectImageFromGallery() {
        if (!isBottomSheetOpen) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityResultForProfilePhoto.launch(intent)
            isBottomSheetOpen = true
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
        viewModel.userId.observe(viewLifecycleOwner) {
            val photoId = listOfProfile[Random.nextInt(0, 5)]
            val photo = BitmapFactory.decodeResource(resources, photoId)
            if (it is PresentationLayerResponse.Success) {
                vm.addProfile(it.data, photo)
            }
        }

    }
}