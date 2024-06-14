package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.GroupProfilePageBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.adapter.UserListingAdapter
import com.example.kanakubook.pre.fragment.MultiUserPickListFragment
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.GroupViewModel
import com.example.kanakubook.pre.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupProfilePageActivity : AppCompatActivity() {

    private lateinit var binding: GroupProfilePageBinding
    private lateinit var adapter: UserListingAdapter
    private val preferenceHelper = PreferenceHelper(this)
    private val groupViewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private val userViewModel: UserViewModel by viewModels { UserViewModel.FACTORY }
    private lateinit var groupName: String
    private var groupId: Long = -1
    private lateinit var membersId: List<Long>
    private var members: List<UserProfileSummary>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in_center, R.anim.dont_slide)
        initialSetUp()
        setListener()
        setObserver()
        getMembersDetail()
    }

    private fun setListener() {
        binding.close.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.dont_slide, R.anim.fade_out_center)
        }

        binding.addGroup.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag("multiUserPickList") != null) {
                return@setOnClickListener
            }
            val bottomSheet = MultiUserPickListFragment()
            bottomSheet.isForBottomSheet = true
            bottomSheet.arguments = Bundle().apply {
                putLong("userId", getLoggedUserId())
                putLongArray("membersId", membersId.toLongArray())
            }
            bottomSheet.show(supportFragmentManager, "multiUserPickList")

        }
    }

    private fun setObserver() {
        userViewModel.userData.observe(this) {
            members = it
            membersId = it.map {id -> id.userId }

            binding.groupMemberTitle.text = "Group members (${membersId.size})"
            if (members.isNullOrEmpty()) {
                Toast.makeText(this, "membersNotFound", Toast.LENGTH_SHORT).show()
            } else {
                adapter.updateData(members!!)
            }
        }

        supportFragmentManager.setFragmentResultListener("addFriend", this) { _, _ ->
            showLoading()
            groupViewModel.addMembers(groupId, viewModel.selectedList.map {
                it.userId
            })
        }

        groupViewModel.addMembersResponse.observe(this) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    val list = membersId.toMutableList()
                    viewModel.selectedList.forEach { id ->
                        list.add(id.userId)
                    }
                    membersId = list
                    viewModel.selectedList = emptyList<MultiUserPickListFragment.MySelectableUserData>().toMutableList()
                    viewModel.listOfMySelectableUserData = emptyList()
                    getMembersDetail()
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this, "can't add members right now", Toast.LENGTH_SHORT).show()
                }
            }
            hideLoading()
        }
    }

    private fun initialSetUp() {
        binding = GroupProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        getGroupProfileData()
        setGroupData()
        getGroupProfile()
        setAdapter()
    }

    private fun setGroupData() {
        binding.name.text = groupName
        val membersCountTitle = "Group members (${membersId.size})"
        binding.groupMemberTitle.text = membersCountTitle
    }

    private fun getGroupProfileData() {
        groupId = intent.getLongExtra("groupId", -1)
        groupName = intent.getStringExtra("name") ?: "- empty -"
        val bundle = intent.getBundleExtra("bundleFromDetailPage")
        bundle?.let {
            it.getLongArray("members")?.toList()?.let { data ->
                membersId = data
            }
        }
    }

    private fun getMembersDetail() {
        userViewModel.getUser(membersId)
    }

    private fun getGroupProfile() {
        if (groupId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = groupViewModel.getProfile(groupId)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        binding.profile.setImageBitmap(bitmap)
                    } else {
                        binding.profile.setImageResource(R.drawable.default_group_profile12)
                    }
                }
            }
        }
    }

    private fun setAdapter() {
        adapter = UserListingAdapter(object : UserListingAdapter.Callback {
            override suspend fun getImage(userId: Long): Bitmap? {
                return viewModel.getProfile(userId)
            }

            override fun clickListener(user: UserProfileSummary) {

            }
        })
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
    }

    private fun getLoggedUserId(): Long {
        if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            val userId = preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
            return userId
        } else {
            val intent = Intent(this, AppEntryPoint::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            return -1
        }
    }


    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
    }

    override fun finish() {
        setResult(RESULT_OK,intent.putExtra("membersId",membersId.toLongArray()))
        super.finish()
    }
}