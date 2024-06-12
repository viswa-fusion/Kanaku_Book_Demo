package com.example.kanakubook.pre.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ProfileEditActivityBinding
import com.example.kanakubook.databinding.ProfilePageActivityBinding
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class EditProfileActivity: AppCompatActivity(R.layout.profile_edit_activity) {

    private lateinit var binding: ProfileEditActivityBinding
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var isBottomSheetOpen = false
    private var profileUri : Uri? = null
    private val PROFILE_URI_KEY = "person profile"

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    profileUri = selectedImageUri
                    binding.profile.setImageURI(selectedImageUri)
                }
            }
            isBottomSheetOpen = false
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
    }

    private fun initialSetUp() {
        binding = ProfileEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)

        val userId = intent.getLongExtra("userId",-1)
        val name = intent.getStringExtra("name")
        val phone = intent.getLongExtra("phone",-1)

        if(profileUri != null){
            binding.profile.setImageURI(profileUri)
        }
        else if(viewModel.profileImage != null){
            binding.profile.setImageBitmap(viewModel.profileImage)
        }else {
            lifecycleScope.launch(Dispatchers.IO) {
                val profile = viewModel.getProfile(userId)
                withContext(Dispatchers.Main) {
                    viewModel.profileImage = profile
                    viewModel.profileImage?.let { binding.profile.setImageBitmap(viewModel.profileImage) }
                }
            }
        }

        binding.nameText.setText(name)
        val phoneFormat = if (phone != -1L) "+91 $phone" else "- empty -"
        binding.phoneNumber.setText(phoneFormat)

    }

    private fun validation(): Boolean {
        return (binding.name.isErrorEnabled)
    }

    private fun setListener(){
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

            validateName(binding.nameText.text.toString())


            if(validation()){

            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
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
            binding.profile.setImageURI(this.profileUri)
        }
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

    private fun validateName(name: String) {
        if (name.matches("^[a-zA-Z]+(?:\\s[a-zA-Z]+)+$".toRegex())) {
            binding.name.error = "Name must be at least 3 characters long"
        } else {
            binding.name.error = null
        }
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