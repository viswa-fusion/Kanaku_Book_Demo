package com.example.kanakubook.pre.fragment


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
                binding.editTextPhoneNumber.text.toString().toLong(),
                binding.editTextPassword.text.toString()
            )
        }

        binding.signUp.setOnClickListener {
            parentFragmentManager.commit {
                add(R.id.fragment_container_view, SignUpScreenFragment())
                addToBackStack("signUpFragment")
            }
        }
    }

    private fun setObserver() {
        viewModel.userDataDetails.observe(viewLifecycleOwner){
            when(it){
                is PresentationLayerResponse.Success -> {
                    preferenceHelper.writeLongToPreference(KanakuBookApplication.PREF_USER_ID,it.data.userId)
                    preferenceHelper.writeBooleanToPreference(KanakuBookApplication.PREF_IS_USER_LOGIN,true)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("userId", it.data.userId)
                    Log.i(TAG,"data : ${it.data}")
                    requireActivity().startActivity(intent)
                    requireActivity().finish()
                }

                is PresentationLayerResponse.Error -> {
                    Log.i(TAG,"data : ${it.message}")
                    Toast.makeText(context,"error :  ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}