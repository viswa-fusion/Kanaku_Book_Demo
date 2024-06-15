package com.example.kanakubook.pre.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.LoginScreenFragmentBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.MainActivity
import com.example.kanakubook.pre.viewmodel.LoginViewModel
import com.example.kanakubook.util.FieldValidator

class LoginScreenFragment : Fragment(R.layout.login_screen_fragment) {

    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var binding: LoginScreenFragmentBinding
    private val validator by lazy { FieldValidator() }
    private val viewModel: LoginViewModel by viewModels { LoginViewModel.FACTORY }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginScreenFragmentBinding.bind(view)
        setObserver()
        setListeners()
        setupValidation()
    }

    private fun setListeners() {
        binding.signUp.setOnClickListener {
            parentFragmentManager.commit {
                val frag = SignUpScreenFragment()
                replace(R.id.fragment_container_view, frag)
                addToBackStack("signUpFragment")
            }
        }
        setTextChangeListener()
    }

    private fun setupValidation() {
        binding.buttonLogIn.setOnClickListener {
            val phoneNumber = binding.editTextPhoneNumber.text.toString()
            val password = binding.editTextPassword.text.toString()
            val phoneError = validator.validatePhoneNumber(phoneNumber,false)
            val passwordError = validator.validatePassword(password)

            if (phoneError != null) {
                binding.layoutPhoneNumber.error = phoneError
                if (!viewModel.isNotFirstTimeValidation) viewModel.isNotFirstTimeValidation = true
                return@setOnClickListener
            } else {
                binding.layoutPhoneNumber.error = null
            }

            if (passwordError.errorMessage != null) {
                binding.layoutPassword.error = passwordError.errorMessage
                if (!viewModel.isNotFirstTimeValidation) viewModel.isNotFirstTimeValidation = true
                return@setOnClickListener
            } else {
                binding.layoutPassword.error = null
            }

            viewModel.authenticateUser(
                binding.editTextPhoneNumber.text.toString().toLong(),
                binding.editTextPassword.text.toString()
            )
        }
    }

    private fun setTextChangeListener() {

        binding.editTextPhoneNumber.addTextChangedListener {
            if (viewModel.isNotFirstTimeValidation) {
                val phoneNumber = it.toString()
                val phoneError = validator.validatePhoneNumber(phoneNumber,false)
                binding.layoutPhoneNumber.error = phoneError
            }
        }

        binding.editTextPassword.addTextChangedListener {
            if (viewModel.isNotFirstTimeValidation) {
                val password = it.toString()
                val passwordError = validator.validatePassword(password)
                binding.layoutPassword.error = passwordError.errorMessage
            }
        }
    }

    private fun setObserver() {
        viewModel.userDataDetails.observe(viewLifecycleOwner) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    login(it.data.userId)
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                }
            }
        }
        requireActivity().supportFragmentManager.setFragmentResultListener(
            "userIdFromSignUp",
            viewLifecycleOwner
        ) { a, b ->
            if (a == "userIdFromSignUp") {
                val userId = b.getLong("userId")
                if (userId > 0) login(userId)
            }
        }

    }

    private fun login(userId: Long) {
        preferenceHelper.writeLongToPreference(KanakuBookApplication.PREF_USER_ID, userId)
        preferenceHelper.writeBooleanToPreference(KanakuBookApplication.PREF_IS_USER_LOGIN, true)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("userId", userId)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }
}