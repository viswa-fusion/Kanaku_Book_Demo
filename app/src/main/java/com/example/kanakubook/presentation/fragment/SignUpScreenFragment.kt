package com.example.kanakubook.presentation.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.SignUpScreenFragmentBinding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.activity.MainActivity
import com.example.kanakubook.presentation.viewmodel.SignUpViewModel
import com.example.kanakubook.util.FieldValidator
import java.util.Calendar


class SignUpScreenFragment : Fragment(R.layout.sign_up_screen_fragment) {

    private lateinit var binding: SignUpScreenFragmentBinding
    private lateinit var preferenceHelper: PreferenceHelper
    private val validator by lazy { FieldValidator() }
    private val viewModel: SignUpViewModel by viewModels { SignUpViewModel.FACTORY }
    private var profileUri: Uri? = null
    private val PROFILE_URI_KEY = "profile_uri"
    private var isBottomSheetOpen = false


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
        setupValidation()
    }

    private fun setupValidation() {
        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val repeatPassword = binding.editTextRepeatPassword.text.toString().trim()

            val nameError = validator.validateName(name)
            val phoneError = validator.validatePhoneNumber(phoneNumber)
            val passwordError = validator.validatePassword(password)

            if (nameError != null) {
                binding.layoutName.error = nameError
                if(!viewModel.isNotFirstTimeValidation)viewModel.isNotFirstTimeValidation = true
                if(binding.editTextName.requestFocus()) {
                    requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
                return@setOnClickListener
            } else {
                binding.layoutName.error = null
            }

            if (phoneError != null) {
                binding.layoutPhoneNumber.error = phoneError
                if(!viewModel.isNotFirstTimeValidation)viewModel.isNotFirstTimeValidation = true
                binding.editTextPhoneNumber.requestFocus()
                return@setOnClickListener
            } else {
                binding.layoutPhoneNumber.error = null
            }

            if (passwordError.errorMessage != null) {
                binding.layoutPassword.error = passwordError.errorMessage
                binding.passwordStrengthIndicator.text = passwordError.strength.text
                binding.passwordStrengthProgress.setProgressCompat(passwordError.strength.indicator,true)
                if(!viewModel.isNotFirstTimeValidation)viewModel.isNotFirstTimeValidation = true
                binding.editTextRepeatPassword.requestFocus()
                return@setOnClickListener
            } else {
                binding.passwordStrengthIndicator.text = passwordError.strength.toString()
                binding.passwordStrengthProgress.progress = passwordError.strength.indicator
                binding.layoutPassword.error = null
            }

            if (password != repeatPassword) {
                binding.layoutRepeatPassword.error = "repeat passwords do not match with password"
                if(!viewModel.isNotFirstTimeValidation)viewModel.isNotFirstTimeValidation = true
                binding.editTextRepeatPassword.requestFocus()
                return@setOnClickListener
            } else {
                binding.layoutRepeatPassword.error = null
            }

            showLoading()
            val cleanedPhoneNumber = phoneNumber.replace(Regex("[+]"), "")
            val dob = binding.dateOfBirth.text.toString().ifEmpty { null }
            viewModel.signUp(name, cleanedPhoneNumber.toLong(),dob, password, repeatPassword)
        }
    }
    private fun setListener() {
        binding.dateOfBirth.setOnClickListener {
            showDatePicker()
        }
        binding.imageProfile.setOnClickListener {
            selectImageFromGallery()
        }
        binding.goBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setTextChangeListener()
    }

    private fun setTextChangeListener() {
        binding.editTextName.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) openKeyboard(v)
        }
        binding.editTextPhoneNumber.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) openKeyboard(v)
        }

        binding.editTextPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) openKeyboard(v)
        }

        binding.editTextName.addTextChangedListener {
            if (viewModel.isNotFirstTimeValidation) {
                val name = it.toString()
                val nameError = validator.validateName(name)
                binding.layoutName.error = nameError
            }
        }

        binding.editTextPhoneNumber.addTextChangedListener {
            if (viewModel.isNotFirstTimeValidation) {
                val phoneNumber = it.toString()
                val phoneError = validator.validatePhoneNumber(phoneNumber)
                binding.layoutPhoneNumber.error = phoneError
            }
        }

        binding.editTextPassword.addTextChangedListener{
            binding.passwordStrength.visibility = View.VISIBLE
            val password = it.toString()
            val passwordError = validator.validatePassword(password)
            binding.passwordStrengthIndicator.text = passwordError.strength.text
            binding.passwordStrengthProgress.setIndicatorColor(setProgressBarColor(passwordError.strength))
            binding.passwordStrengthProgress.setProgressCompat(passwordError.strength.indicator,true)
                if (viewModel.isNotFirstTimeValidation){
                    binding.layoutPassword.error = passwordError.errorMessage
                }
        }

        binding.editTextRepeatPassword.addTextChangedListener{
                val repeatPassword = it.toString()
                val password = binding.editTextPassword.text.toString()
                if (password != repeatPassword) {
                    binding.layoutRepeatPassword.error = "repeat passwords do not match with password"
                } else {
                    binding.layoutRepeatPassword.error = null
                }
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
            hideLoading()
            when (it) {
                is PresentationLayerResponse.Success -> {
                    preferenceHelper.writeLongToPreference(
                        KanakuBookApplication.PREF_USER_ID,
                        it.data
                    )
                    preferenceHelper.writeBooleanToPreference(
                        KanakuBookApplication.PREF_IS_USER_LOGIN,
                        true
                    )
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("userId", it.data)
                    requireActivity().startActivity(intent)
                    Toast.makeText(requireActivity(), "Signup successful", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().finish()
                }

                is PresentationLayerResponse.Error -> {
                    if (it.message == "user exist"){
                        binding.layoutPhoneNumber.error = "phone number already exist"
                    }
                    Toast.makeText(requireActivity(), "Signup failed", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireActivity(),
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

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
    }

    private fun setProgressBarColor(strength : FieldValidator.PasswordStrength): Int{
        return when (strength) {
            FieldValidator.PasswordStrength.VERY_WEAK -> ContextCompat.getColor(requireActivity(), R.color.progress_color_very_weak)
            FieldValidator.PasswordStrength.WEAK -> ContextCompat.getColor(requireActivity(), R.color.progress_color_weak)
            FieldValidator.PasswordStrength.REASONABLE -> ContextCompat.getColor(requireActivity(), R.color.progress_color_reasonable)
            FieldValidator.PasswordStrength.MEDIUM -> ContextCompat.getColor(requireActivity(), R.color.progress_color_medium)
            FieldValidator.PasswordStrength.STRONG -> ContextCompat.getColor(requireActivity(), R.color.progress_color_strong)
        }
    }

    private fun openKeyboard(view: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}