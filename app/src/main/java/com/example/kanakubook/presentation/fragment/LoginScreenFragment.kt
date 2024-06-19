package com.example.kanakubook.presentation.fragment


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
import com.example.domain.repository.response.DataLayerResponse
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.LoginScreenFragmentBinding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.activity.MainActivity
import com.example.kanakubook.presentation.viewmodel.LoginViewModel
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
            try{
                val num = binding.editTextPhoneNumber.text.toString().toLong()
                viewModel.authenticateUser(num, binding.editTextPassword.text.toString())
            }catch (e:Exception){
                binding.layoutPhoneNumber.error = "invalid credential"
                return@setOnClickListener
            }

        }
    }

    private fun setTextChangeListener() {

        binding.editTextPhoneNumber.addTextChangedListener {
            if (viewModel.isNotFirstTimeValidation) {
                val phoneNumber = it.toString()
                val phoneError = validator.validatePhoneNumber(phoneNumber, false)
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
                    when {
                        it.message == "wrong password" -> {
                            binding.layoutPhoneNumber.error = null
                            binding.layoutPassword.error = "incorrect password"
                        }

                        it.message == "user not found" -> {
                            binding.layoutPhoneNumber.error = "user not found"
                        }

                        else -> {
                            Toast.makeText(context, "login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
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