package com.example.kanakubook.pre.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MainScreenFragmentBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.AddExpenseActivity
import com.example.kanakubook.pre.activity.AppEntryPoint
import com.example.kanakubook.pre.activity.MainActivity
import com.example.kanakubook.pre.activity.ProfileActivity
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.LoginViewModel
import com.google.android.material.search.SearchBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class BaseHomeFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected lateinit var binding: MainScreenFragmentBinding
    private val viewModel : LoginViewModel by viewModels{ LoginViewModel.FACTORY }
    private val userViewModel : FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private lateinit var userData : UserProfileData
    private var profileImage: Bitmap? = null
    private lateinit var preferenceHelper : PreferenceHelper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }
    private val activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        binding.createFab.callOnClick()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainScreenFragmentBinding.bind(view)
        setObserver()
        viewModel.getLoggedUser(getLoggedUserId())

        if (profileImage != null) {
            binding.imageProfile.setImageBitmap(profileImage)
        } else {
            binding.imageProfile.setImageResource(R.drawable.default_profile_image)
        }

        binding.imageProfile.setOnClickListener {
            val intent = Intent(requireActivity(), ProfileActivity::class.java)
            intent.putExtra("userId", getLoggedUserId())
            intent.putExtra("name", userData.name)
            intent.putExtra("phone", userData.phone)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.dont_slide)
        }

        binding.createExpense.setOnClickListener {
            val intent = Intent(requireActivity(), AddExpenseActivity::class.java)
            activityResult.launch(intent)
        }



    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }


    private fun setObserver() {
        viewModel.loggedUserProfile.observe(viewLifecycleOwner){
            when(it){
                is PresentationLayerResponse.Success -> {
                    userData = it.data
                    if (profileImage == null){
                        CoroutineScope(Dispatchers.IO).launch {
                            profileImage = userViewModel.getProfile(it.data.userId)
                            withContext(Dispatchers.Main){
                                profileImage?.let{ binding.imageProfile.setImageBitmap(profileImage) }
                            }
                        }
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(requireActivity(),"something went wrong",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    fun getLoggedUserId(): Long {
        if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            val userId = preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
            return userId
        } else {
            val intent = Intent(requireActivity(), AppEntryPoint::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
            return -1
        }
    }
}