package com.example.kanakubook.pre.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MainScreenFragmentBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.AddFriendActivity
import com.example.kanakubook.pre.activity.AddGroupActivity
import com.example.kanakubook.pre.activity.AppEntryPoint
import com.example.kanakubook.pre.adapter.GroupsListAdapter
import com.example.kanakubook.pre.viewmodel.FabViewModel
import com.example.kanakubook.pre.viewmodel.GroupViewModel
import com.example.kanakubook.util.CustomAnimationUtil

class HomeFragment : BaseHomeFragment(R.layout.main_screen_fragment) {

    private val fabViewModel: FabViewModel by viewModels()
    private var isFabRotated = false
    private val FAB_STATE = "fab state"
    private lateinit var preferenceHelper: PreferenceHelper
    private val viewModel : GroupViewModel by viewModels { GroupViewModel.FACTORY }

    private val addGroupResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                getGroupList()
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getGroupList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetUp()
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

    private fun setListener(){
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

        binding.addGroup.setOnClickListener {
            val intent = Intent(requireActivity(), AddGroupActivity::class.java)
            addGroupResultLauncher.launch(intent)
            binding.createFab.callOnClick()
        }
    }

    private fun setObserver(){
        viewModel.getAllGroups.observe(viewLifecycleOwner){
            when(it){
                is PresentationLayerResponse.Success ->{
                    if(it.data.isEmpty()){
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    }
                    else{
                        binding.emptyTemplate.emptyTemplate.visibility = View.GONE
                        val adapter = binding.recyclerview.adapter
                        if (adapter is GroupsListAdapter) {
                            adapter.updateData(it.data)
                        }
                    }
                }
                is PresentationLayerResponse.Error ->{
                    Toast.makeText(requireActivity(), "fail fetching", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fabViewModel.fabVisibility.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible) {
                CustomAnimationUtil.showFABs(
                    binding.addGroup,
                    binding.createFriend,
                    binding.createExpense,
                    CustomAnimationUtil.GROUP_SCREEN_FAB
                )
            } else {
                CustomAnimationUtil.hideFABs(
                    binding.addGroup,
                    binding.createFriend,
                    binding.createExpense
                )
            }
        }

    }

    private fun getGroupList() {
        if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            val userId = preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
            viewModel.getAllMyGroups(userId)
        } else {
            val intent = Intent(requireActivity(), AppEntryPoint::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }


    private fun initialSetUp(){
        binding.recyclerview.adapter = GroupsListAdapter{ groupId ->
            return@GroupsListAdapter viewModel.getProfile(groupId)
        }
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
    }
}