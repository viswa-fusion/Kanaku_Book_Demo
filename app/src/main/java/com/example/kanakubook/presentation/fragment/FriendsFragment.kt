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
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.activity.AddFriendActivity
import com.example.kanakubook.presentation.activity.AppEntryPoint
import com.example.kanakubook.presentation.activity.FriendDetailPageActivity
import com.example.kanakubook.presentation.adapter.FriendsProfileListAdapter
import com.example.kanakubook.presentation.viewmodel.CommonViewModel
import com.example.kanakubook.presentation.viewmodel.FabViewModel
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.util.Constants
import com.example.kanakubook.util.CustomAnimationUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.search.SearchView
import java.util.Locale


class FriendsFragment : BaseHomeFragment(R.layout.main_screen_fragment) {

    private lateinit var preferenceHelper: PreferenceHelper
    private var isFabRotated = false
    private val fabViewModel: FabViewModel by activityViewModels()

    private val FAB_STATE = "fab state"
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private val commonViewModel: CommonViewModel by activityViewModels()
    private lateinit var adapter: FriendsProfileListAdapter

    private var layoutTag: String = Constants.NORMAL_LAYOUT
    private var withoutToolBar: Boolean = false


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
        Log.i("check", "data: friend:oncreate")

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            savedInstanceState.getString("layoutTag")?.let {
                layoutTag = it
            }
            savedInstanceState.getBoolean("withoutToolbar")
        }
        layoutInitialSetup()
        setListener()
        setObserver()
        showLoading()
        getFriendsList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FAB_STATE, isFabRotated)
        outState.putBoolean("withoutToolbar", withoutToolBar)
        outState.putString("layoutTag", layoutTag)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(FAB_STATE))
                binding.createFab.callOnClick()
        }
        if(withoutToolBar && commonViewModel.isVisible){
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.visibility =
                View.GONE
            activity?.findViewById<FloatingActionButton>(R.id.createFab)?.visibility =
                View.GONE
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


        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })

        if (!withoutToolBar){
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

        needReload.observe(requireActivity()){
            if(it){
                getFriendsList()
            }
        }

        viewModel.friendsList.observe(viewLifecycleOwner) {
            hideLoading()
            when (it) {
                is PresentationLayerResponse.Success -> {
                    if (it.data.isEmpty()) {
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    } else {
                        binding.emptyTemplate.emptyTemplate.visibility = View.GONE
                        val adapter = binding.recyclerview.adapter
                        if (adapter is FriendsProfileListAdapter) {
                            val data =
                                if (layoutTag != Constants.NORMAL_LAYOUT || withoutToolBar) it.data.sortedBy { u -> u.name } else it.data
                            commonViewModel.listUserData = data
                            val filteredUsers =
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
                            if (filteredUsers.isEmpty()) {
                                binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
                            } else {
                                binding.searchNotFound.emptyTemplate.visibility = View.GONE
                                    adapter.updateData(filteredUsers)

                            }
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

    override fun onStop() {
        super.onStop()
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
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    fun setTag(tag: String) {
        layoutTag = tag
    }
    fun setWithoutToolbarTrue(){
        withoutToolBar = true
    }
    private fun layoutInitialSetup() {
        checkLayoutNeed()
        binding.emptyTemplate.content?.text = "You have no connection at this \n moment"
        binding.boxesContainer.visibility = View.INVISIBLE
        adapter = FriendsProfileListAdapter(requireActivity(),
            object : FriendsProfileListAdapter.Callbacks {
                override suspend fun getImage(userId: Long): Bitmap? {
                    return viewModel.getProfile(userId)
                }

                override fun onClickItemListener(userProfileSummary: UserProfileSummary, view:View) {
                    when (layoutTag) {
                        Constants.NORMAL_LAYOUT -> {
                            val mainPair = Pair(view, "cardAnimationT")
                            val pairs = arrayOf( mainPair)
                            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), *pairs)

                            val intent = Intent(requireActivity(), FriendDetailPageActivity::class.java)
                            intent.putExtra("name", userProfileSummary.name)
                            intent.putExtra("phone", userProfileSummary.phone)
                            intent.putExtra("userId", userProfileSummary.userId)
                            intent.putExtra("connectionId", userProfileSummary.connectionId)


                            addFriendResultLauncher.launch(intent,bundle)
                        }

                        Constants.FOR_TAB_LAYOUT -> {
                            showLoading()
                            commonViewModel.selectSplitWithListener.value =
                                CommonViewModel.SelectionData(
                                    listOf(getLoggedUserId(), userProfileSummary.userId),
                                    userProfileSummary.connectionId!!,
                                    true
                                )
                        }
                    }
                }


                override fun clickImage(drawable: Drawable?) {
                    showEnlargedImage(drawable)
                }
            })
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
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


    fun filterFriends(query: String) {
        if (!isAdded || activity == null) {
            return
        }

        val data = if (layoutTag != Constants.NORMAL_LAYOUT || withoutToolBar) commonViewModel.listUserData.sortedBy { u -> u.name } else commonViewModel.listUserData
        val filteredFriends =
            data.filter { it.name.contains(query, ignoreCase = true) }

        if (filteredFriends.isEmpty()) {
            binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
        } else {
            binding.searchNotFound.emptyTemplate.visibility = View.GONE
            adapter.highlightText(query)
            adapter.updateData(filteredFriends)
        }

    }

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
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






fun main(){

    val w1 = A("a",1)
    val w2 = A("b",1)

    val s1 = Person("a",1)
    val s2 = Person("b",1)

    println(w1 == w2)
    println(s1 == s2)
}
class A(val name:String,val age :Int)

data class Person(val name :String,val age:Int)