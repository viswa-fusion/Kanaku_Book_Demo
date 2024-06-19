package com.example.kanakubook.presentation.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MainScreenFragmentBinding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.activity.AddExpenseActivity
import com.example.kanakubook.presentation.activity.AppEntryPoint
import com.example.kanakubook.presentation.activity.ProfileActivity
import com.example.kanakubook.presentation.viewmodel.CommonViewModel
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.presentation.viewmodel.LoginViewModel
import com.example.kanakubook.util.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.search.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class BaseHomeFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected lateinit var binding: MainScreenFragmentBinding
    private val viewModel: LoginViewModel by viewModels { LoginViewModel.FACTORY }
    private val userViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private lateinit var userData: UserProfileData
    private var profileImage: Bitmap? = null
    private lateinit var preferenceHelper: PreferenceHelper



    private val fragment: ViewPagerFragment by lazy {
        ViewPagerFragment(
            Constants.NORMAL_LAYOUT
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            binding.createFab.callOnClick()
        }

    override fun onResume() {
        super.onResume()
        viewModel.getLoggedUser(getLoggedUserId())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainScreenFragmentBinding.bind(view)

        setObserver()
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

        val searchBar = binding.searchBar
        val homeScreenSearchView1 = binding.homeScreenSearchView1
        homeScreenSearchView1.setupWithSearchBar(searchBar)

        childFragmentManager.commit {
            replace(R.id.search_view_fragment_container, fragment)
        }
        homeScreenSearchView1.editText.addTextChangedListener {
            filterViewPagerFragments(it.toString())
        }
        homeScreenSearchView1.toolbar.setBackgroundColor(requireActivity().getColor(R.color.white))

    }



    private fun filterViewPagerFragments(query: String) {
        fragment.filterData(query)
    }

    private fun setObserver() {
        viewModel.loggedUserProfile.observe(viewLifecycleOwner) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    userData = it.data
                    if (profileImage == null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            profileImage = userViewModel.getProfile(it.data.userId)
                            withContext(Dispatchers.Main) {
                                profileImage?.let { binding.imageProfile.setImageBitmap(profileImage) }
                            }
                        }
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(requireActivity(), "something went wrong", Toast.LENGTH_SHORT)
                        .show()
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