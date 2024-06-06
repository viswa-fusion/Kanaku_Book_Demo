package com.example.kanakubook.pre.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.LoginScreenFragmentBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.MainActivity
import com.example.kanakubook.pre.viewmodel.LoginViewModel

class LoginScreenFragment: Fragment(R.layout.login_screen_fragment) {

    private lateinit var preferenceHelper :PreferenceHelper
    private lateinit var binding: LoginScreenFragmentBinding
    private val viewModel: LoginViewModel by viewModels { LoginViewModel.FACTORY}
    private val TAG = "Tag"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = LoginScreenFragmentBinding.bind(view)
        setObserver()

        binding.buttonLogin.setOnClickListener {
            viewModel.authenticateUser(
                binding.amount.text.toString().toLong(),
                binding.editTextPassword.text.toString()
            )
        }

        binding.signUp.setOnClickListener {
            parentFragmentManager.commit {
                val frag = SignUpScreenFragment()
                replace(R.id.fragment_container_view, frag)
                addToBackStack("signUpFragment")
            }
        }
    }

    private fun setObserver() {
        viewModel.userDataDetails.observe(viewLifecycleOwner){
            when(it){
                is PresentationLayerResponse.Success -> {
                   login(it.data.userId)
                    Toast.makeText(context,"Login successful!", Toast.LENGTH_SHORT).show()
                }

                is PresentationLayerResponse.Error -> {
                    Log.i(TAG,"data : ${it.message}")
                    Toast.makeText(context,"error", Toast.LENGTH_SHORT).show()
                }
            }
        }
        requireActivity().supportFragmentManager.setFragmentResultListener("userIdFromSignUp",viewLifecycleOwner){a,b ->
            if(a == "userIdFromSignUp"){
                val userId = b.getLong("userId")
                if (userId > 0) login(userId)
            }
        }

    }

    private fun login(userId: Long){
        preferenceHelper.writeLongToPreference(KanakuBookApplication.PREF_USER_ID,userId)
        preferenceHelper.writeBooleanToPreference(KanakuBookApplication.PREF_IS_USER_LOGIN,true)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("userId", userId)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }
}