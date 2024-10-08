package com.example.kanakubook.presentation.fragment


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.data.util.PreferenceHelper
import com.example.domain.model.GroupData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.activity.AddGroupActivity
import com.example.kanakubook.presentation.activity.AppEntryPoint
import com.example.kanakubook.presentation.activity.GroupDetailPageActivity
import com.example.kanakubook.presentation.adapter.GroupsListAdapter
import com.example.kanakubook.presentation.viewmodel.CommonViewModel
import com.example.kanakubook.presentation.viewmodel.FabViewModel
import com.example.kanakubook.presentation.viewmodel.GroupViewModel
import com.example.kanakubook.util.Constants
import com.example.kanakubook.util.CustomAnimationUtil
import com.example.kanakubook.util.CustomDividerItemDecoration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.search.SearchView

class GroupFragment : BaseHomeFragment(R.layout.main_screen_fragment) {

    private val fabViewModel: FabViewModel by viewModels()
    private val viewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val commonViewModel: CommonViewModel by activityViewModels()

    private var layoutTag: String = Constants.NORMAL_LAYOUT
    private var withoutToolBar: Boolean = false

    private var isFabRotated = false
    private val FAB_STATE = "fab state"


    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var adapter: GroupsListAdapter

    private val addGroupResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                getGroupList()
            }
        }

    fun setTag(tag: String) {
        layoutTag = tag
    }

    fun setWithoutToolbarTrue() {
        withoutToolBar = true
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            savedInstanceState.getString("layoutTag")?.let {
                layoutTag = it
            }
            withoutToolBar = savedInstanceState.getBoolean("withoutToolBar")
        }
        initialSetUp()
        setListener()
        setObserver()
        getGroupList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FAB_STATE, isFabRotated)
        outState.putBoolean("withoutToolBar", withoutToolBar)
        outState.putString("layoutTag", layoutTag)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(FAB_STATE)) {
                binding.createFab.callOnClick()
            }
        }

        if (withoutToolBar && commonViewModel.isVisible) {
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility =
                View.GONE
            activity?.findViewById<FloatingActionButton>(R.id.createFab)?.visibility =
                View.GONE
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

        binding.addGroup.setOnClickListener {
            val intent = Intent(requireActivity(), AddGroupActivity::class.java)
            addGroupResultLauncher.launch(intent)
            binding.createFab.callOnClick()
        }
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })
        if (!withoutToolBar) {
            binding.homeScreenSearchView1.addTransitionListener { searchView, previousState, newState ->
                if (newState == SearchView.TransitionState.SHOWING) {
                    commonViewModel.isVisible = true
                    activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility =
                        View.GONE
                    activity?.findViewById<FloatingActionButton>(R.id.createFab)?.visibility =
                        View.GONE
                }
                if (newState == SearchView.TransitionState.HIDING) {
                    commonViewModel.isVisible = false
                    activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility =
                        View.VISIBLE
                    activity?.findViewById<FloatingActionButton>(R.id.createFab)?.visibility =
                        View.VISIBLE
                }
            }
        }

    }

    fun Fragment.hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferenceHelper = PreferenceHelper(context)
    }


    private fun setObserver() {
        viewModel.getAllGroups.observe(viewLifecycleOwner) {

            when (it) {
                is PresentationLayerResponse.Success -> {
                    if (it.data.isEmpty()) {
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    } else {
                        binding.emptyTemplate.emptyTemplate.visibility = View.INVISIBLE
                        val adapter = binding.recyclerview.adapter
                        if (adapter is GroupsListAdapter) {
                            val data =
                                if (layoutTag != Constants.NORMAL_LAYOUT || withoutToolBar) it.data.sortedBy { u -> u.name } else it.data

                            commonViewModel.listGroupData = data
                            val filteredGroups =
                                if (layoutTag != Constants.NORMAL_LAYOUT || withoutToolBar) {
                                    data.filter { f ->
                                        f.name.contains(
                                            commonViewModel.filterString,
                                            ignoreCase = true
                                        )
                                    }
                                } else {
                                    data
                                }
                            adapter.highlightText(commonViewModel.filterString)
                            if (filteredGroups.isEmpty()) {
                                binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
                            } else {
                                binding.searchNotFound.emptyTemplate.visibility = View.GONE
                                adapter.updateData(filteredGroups)
                            }
                        }
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(requireActivity(), "fail fetching", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        needReload.observe(requireActivity()){
            if(it){
                getGroupList()
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


    private fun initialSetUp() {
        checkLayoutNeed()

        binding.emptyTemplate.content?.text = "You have no groups at this \n moment"
        adapter = GroupsListAdapter(requireActivity(), object : GroupsListAdapter.CallBack {
            override suspend fun getImage(groupId: Long): Bitmap? {
                return viewModel.getProfile(groupId)
            }

            override fun onClickItemListener(groupData: GroupData, view: View) {
                when (layoutTag) {
                    Constants.NORMAL_LAYOUT -> {


                        val bundle1 = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            view, "cardAnimationT"
                        )
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

//                        val profilePair = Pair()
//                        val pairs = arrayOf(profilePair)

                        addGroupResultLauncher.launch(intent, bundle1)

                    }

                    Constants.FOR_TAB_LAYOUT -> {
                        commonViewModel.selectSplitWithListener.value =
                            CommonViewModel.SelectionData(
                                groupData.members.map { it.userId },
                                groupData.id,
                                false
                            )
                    }
                }
            }

            override fun clickImage(drawable: Drawable?, view: View) {
                showEnlargedImage(drawable)
            }
        })
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        val dividerDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.divider)
        binding.recyclerview.addItemDecoration(
            CustomDividerItemDecoration(requireActivity(), dividerDrawable, 200, 16)
        )

    }

    private fun showEnlargedImage(imageDrawable: Drawable?) {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_enlarged_image)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        val enlargedImage = dialog.findViewById<ImageView>(R.id.enlarged_image)
        val close = dialog.findViewById<ShapeableImageView>(R.id.close)
        close.setOnClickListener {
            dialog.dismiss()
        }
        imageDrawable?.let {
            enlargedImage.setImageDrawable(it)
        }
        dialog.show()
    }

    fun filterGroups(query: String) {
        if (!isAdded || activity == null) {
            return
        }
        val data =
            if (layoutTag != Constants.NORMAL_LAYOUT || withoutToolBar) commonViewModel.listGroupData.sortedBy { u -> u.name } else commonViewModel.listGroupData
        commonViewModel.filterString = query
        val filteredGroups = data.filter { it.name.contains(query, ignoreCase = true) }
        if (filteredGroups.isEmpty()) {
            binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
        } else {
            binding.searchNotFound.emptyTemplate.visibility = View.GONE
            adapter.highlightText(query)
            adapter.updateData(filteredGroups)
        }
    }

    private fun checkLayoutNeed() {
        if (layoutTag == Constants.FOR_TAB_LAYOUT) {
            binding.createFab.visibility = View.GONE
            binding.appbar.visibility = View.GONE
        }

        if (withoutToolBar) {
            binding.appbar.visibility = View.GONE
            binding.createFab.visibility = View.GONE
        }
    }
}