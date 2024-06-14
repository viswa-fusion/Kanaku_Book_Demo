package com.example.kanakubook.pre.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.util.PreferenceHelper
import com.example.domain.model.GroupData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.AddGroupActivity
import com.example.kanakubook.pre.activity.AppEntryPoint
import com.example.kanakubook.pre.activity.GroupDetailPageActivity
import com.example.kanakubook.pre.adapter.GroupsListAdapter
import com.example.kanakubook.pre.viewmodel.CommonViewModel
import com.example.kanakubook.pre.viewmodel.FabViewModel
import com.example.kanakubook.pre.viewmodel.GroupViewModel
import com.example.kanakubook.util.Constants
import com.example.kanakubook.util.CustomAnimationUtil
import com.example.kanakubook.util.CustomDividerItemDecoration

class GroupFragment(private val layoutTag:String = Constants.NORMAL_LAYOUT) : BaseHomeFragment(R.layout.main_screen_fragment) {

    private val fabViewModel: FabViewModel by viewModels()
    private var isFabRotated = false
    private val FAB_STATE = "fab state"
    private lateinit var preferenceHelper: PreferenceHelper
    private val viewModel : GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val commonViewModel: CommonViewModel by activityViewModels ()


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

    override fun onResume() {
        super.onResume()
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
                        binding.emptyTemplate.emptyTemplate.visibility = View.INVISIBLE
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
        checkLayoutNeed()
        val adapter = GroupsListAdapter(object : GroupsListAdapter.CallBack{
            override suspend fun getImage(groupId: Long): Bitmap? {
                return viewModel.getProfile(groupId)
            }

            override fun onClickItemListener(groupData: GroupData) {
                when(layoutTag){
                     Constants.NORMAL_LAYOUT -> {
                        val intent = Intent(requireActivity(), GroupDetailPageActivity::class.java)
                        intent.putExtra("groupName", groupData.name)
                        intent.putExtra("groupId", groupData.id)
                        intent.putExtra("createdBy", groupData.createdBy)
                        val bundle = Bundle()
                        bundle.putLongArray(
                            "members",
                            groupData.members.map { it.userId }.toLongArray()
                        )
                        intent.putExtra("bundle", bundle)
                        startActivity(intent)
                    }
                    Constants.FOR_TAB_LAYOUT -> {
                        commonViewModel.selectSplitWithListener.value = CommonViewModel.SelectionData(
                            groupData.members.map { it.userId },
                            groupData.id,
                            false
                        )
                    }
                }
            }
        })
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        val dividerDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.divider)
        binding.recyclerview.addItemDecoration(
            CustomDividerItemDecoration(requireActivity(), dividerDrawable, 200,16)
        )
    }

    private fun checkLayoutNeed() {
        if(layoutTag == Constants.FOR_TAB_LAYOUT){
            binding.createFab.visibility = View.GONE
            binding.appbar.visibility = View.GONE
        }
    }
}