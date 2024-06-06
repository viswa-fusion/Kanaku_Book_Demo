package com.example.kanakubook.pre.fragment

import android.app.Activity
import android.content.Context
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
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.SignUpScreenFragmentBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.MainActivity
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.SignUpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random


class SignUpScreenFragment : Fragment(R.layout.sign_up_screen_fragment) {

    private lateinit var binding: SignUpScreenFragmentBinding
    private lateinit var preferenceHelper : PreferenceHelper

    private val viewModel: SignUpViewModel by viewModels { SignUpViewModel.FACTORY }
    private var profileUri: Uri? = null
    private val PROFILE_URI_KEY = "profile_uri"
    private var isBottomSheetOpen = false
    private val TAG = "Tag"


    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }

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
        setListener()
    }

    private fun setListener() {
        binding.imageProfile.setOnClickListener {
            selectImageFromGallery()
        }
        binding.buttonSignUp.setOnClickListener {
            showLoading()
            viewModel.signUp(
                binding.editTextName.text.toString(),
                binding.editTextPhoneNumber.text.toString().toLong(),
                binding.editTextPassword.text.toString(),
                binding.editTextRepeatPassword.text.toString(),
            )
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
        viewModel.userId.observe(viewLifecycleOwner) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    hideLoading()
                    preferenceHelper.writeLongToPreference(KanakuBookApplication.PREF_USER_ID,it.data)
                    preferenceHelper.writeBooleanToPreference(KanakuBookApplication.PREF_IS_USER_LOGIN,true)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("userId", it.data)
                    requireActivity().startActivity(intent)
                    Toast.makeText(requireActivity(),"Signup successful",Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(requireActivity(),"Signup failed",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
    }


}