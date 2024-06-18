package com.example.kanakubook.presentation.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ProfileEditActivityBinding
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.util.FieldValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class EditProfileActivity : AppCompatActivity(R.layout.profile_edit_activity) {

    private lateinit var binding: ProfileEditActivityBinding
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var isBottomSheetOpen = false
    private val validator by lazy { FieldValidator() }
    private val PROFILE_URI_KEY = "person profile"

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    viewModel.profileUri = selectedImageUri
                    binding.profile.setImageURI(selectedImageUri)
                }
            }
            isBottomSheetOpen = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()
    }

    private fun setObserver() {
        viewModel.userUpdateResponse.observe(this) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                    Toast.makeText(this, "successfully Profile edited", Toast.LENGTH_SHORT).show()
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this, "edit Profile failed try later", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initialSetUp() {
        binding = ProfileEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)

        viewModel.userId = intent.getLongExtra("userId", -1)
        viewModel.name = intent.getStringExtra("name")
        viewModel.phone = intent.getLongExtra("phone", -1)

        if (viewModel.profileUri != null) {
            binding.profile.setImageURI(viewModel.profileUri)
        } else if (viewModel.profileImage != null) {
            binding.profile.setImageBitmap(viewModel.profileImage)
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val profile = viewModel.getProfile(viewModel.userId!!)
                withContext(Dispatchers.Main) {
                    viewModel.profileImage = profile
                    viewModel.profileImage?.let { binding.profile.setImageBitmap(viewModel.profileImage) }
                }
            }
        }

        binding.nameText.setText(viewModel.name)
        val phoneFormat = if (viewModel.phone != -1L) "+91 ${viewModel.phone}" else "- empty -"
        binding.phoneNumber.setText(phoneFormat)

    }

    private fun setListener() {
        binding.dateOfBirth.setOnClickListener {
            showDatePicker()
        }

        binding.profile.setOnClickListener {
            if (!isBottomSheetOpen) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityResultForProfilePhoto.launch(intent)
                isBottomSheetOpen = true
            }
        }

        binding.editButton.setOnClickListener {
            val name = binding.nameText.text.toString()
            if (!checkOriginal()) {
                val nameError = validator.validateName(name)
                if (nameError != null) {
                    binding.name.error = nameError
                    if (!viewModel.isNotFirstTimeValidation) viewModel.isNotFirstTimeValidation =
                        true
                    binding.nameText.requestFocus()
                    return@setOnClickListener
                } else {
                    binding.name.error = null
                }

                viewModel.updateUser(
                    viewModel.userId!!,
                    name,
                    binding.dateOfBirth.text.toString(),
                    viewModel.profileUri,
                    this
                )
            } else {
                Toast.makeText(this, "no changes", Toast.LENGTH_SHORT).show()
            }
        }

        binding.nameText.addTextChangedListener {
            val name = it.toString()
            val nameError = validator.validateName(name)
            binding.name.error = nameError
        }
    }

    private fun checkOriginal(): Boolean {
        return binding.nameText.text.toString() == viewModel.name && viewModel.profileUri == null
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding.dateOfBirth.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateDateOfBirth(dateOfBirth: String) {

    }

    private fun validateOldPassword(password: String) {

    }

    private fun validateNewPassword(password: String) {

    }


}