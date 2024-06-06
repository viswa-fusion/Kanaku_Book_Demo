package com.example.kanakubook.pre.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.domain.usecase.util.UserProfileSummaryParcel
import com.example.kanakubook.R
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.AddFriendActivity
import com.example.kanakubook.pre.activity.AppEntryPoint
import com.example.kanakubook.pre.activity.FriendDetailPageActivity
import com.example.kanakubook.pre.adapter.FriendsProfileListAdapter
import com.example.kanakubook.pre.viewmodel.FabViewModel
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.util.CustomAnimationUtil


class FriendsFragment : BaseHomeFragment(R.layout.main_screen_fragment) {

    private lateinit var preferenceHelper: PreferenceHelper
    var isFabRotated = false
    private val fabViewModel: FabViewModel by activityViewModels()

    private val FAB_STATE = "fab state"
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }

    private val addFriendResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                getFriendsList()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("check","data: friend:oncreate")
        getFriendsList()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutInitialSetup()
        setListener()
        setObserver()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FAB_STATE, isFabRotated)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(FAB_STATE))
                binding.createFab.callOnClick()
        }
    }


    private fun setListener() {
        binding.createFab.setOnClickListener {
            isFabRotated = CustomAnimationUtil.rotateFab(
                binding.createFab,
                binding.blurFadeScreen,
                isFabRotated
            )
            fabViewModel.setFabVisibility(!fabViewModel.fabVisibility.value!!)
        }

        binding.blurFadeScreen.setOnClickListener {
            binding.createFab.callOnClick()
        }

        binding.createFriend.setOnClickListener {
            val intent = Intent(requireActivity(), AddFriendActivity::class.java)
            addFriendResultLauncher.launch(intent)
            binding.createFab.callOnClick()
        }


    }

    private fun setObserver() {
        fabViewModel.fabVisibility.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible) {
                CustomAnimationUtil.showFABs(
                    binding.addGroup, binding.createFriend,
                    binding.createExpense,
                    CustomAnimationUtil.FRIEND_SCREEN_FAB
                )
            } else {
                CustomAnimationUtil.hideFABs(
                    binding.addGroup,
                    binding.createFriend,
                    binding.createExpense
                )
            }
        }

        viewModel.friendsList.observe(viewLifecycleOwner) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    if (it.data.isEmpty()) {
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    } else {
                        binding.emptyTemplate.emptyTemplate.visibility = View.GONE
                        val adapter = binding.recyclerview.adapter
                        if(adapter is FriendsProfileListAdapter){
                            adapter.updateData(it.data)
                        }
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(requireActivity(), "fail fetching", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isFabRotated) {
            isFabRotated = CustomAnimationUtil.rotateFab(
                binding.createFab,
                binding.blurFadeScreen,
                isFabRotated
            )
            fabViewModel.setFabVisibility(!fabViewModel.fabVisibility.value!!)
        }
    }



    private fun getFriendsList() {
        if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            val userId = preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
            viewModel.getMyFriends(userId)
        } else {
            val intent = Intent(requireActivity(), AppEntryPoint::class.java)
            intent.flags  = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun layoutInitialSetup() {
        binding.boxesContainer.visibility = View.INVISIBLE
        binding.recyclerview.adapter = FriendsProfileListAdapter(object : FriendsProfileListAdapter.Callbacks{
            override suspend fun getImage(userId: Long): Bitmap? {
                return viewModel.getProfile(userId)
            }

            override fun onClickItemListener(userProfileSummary: UserProfileSummary) {
                val intent = Intent(requireActivity(),FriendDetailPageActivity::class.java)
                intent.putExtra("name",userProfileSummary.name)
                intent.putExtra("phone",userProfileSummary.phone)
                intent.putExtra("userId",userProfileSummary.userId)
                val c = userProfileSummary.connectionId
                intent.putExtra("connectionId",userProfileSummary.connectionId)

                startActivity(intent)
            }
        })
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
    }

}
//94349
