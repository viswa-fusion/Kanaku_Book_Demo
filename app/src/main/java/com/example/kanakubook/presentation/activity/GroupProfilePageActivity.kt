package com.example.kanakubook.presentation.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
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
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.adapter.UserListingAdapter
import com.example.kanakubook.presentation.fragment.MultiUserPickListFragment
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.presentation.viewmodel.GroupViewModel
import com.example.kanakubook.presentation.viewmodel.LoginViewModel
import com.example.kanakubook.presentation.viewmodel.UserViewModel
import com.example.kanakubook.util.ImageConversionHelper
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupProfilePageActivity : AppCompatActivity() {

    private lateinit var binding: GroupProfilePageBinding
    private lateinit var adapter: UserListingAdapter
    private val preferenceHelper by lazy { PreferenceHelper(this) }
    private val groupViewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private val userViewModel: UserViewModel by viewModels { UserViewModel.FACTORY }
    private lateinit var groupName: String
    private var groupId: Long = -1
    private lateinit var membersId: List<Long>
    private var createdBy: Long? = null
    private var members: List<UserProfileSummary>? = null

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    groupViewModel.groupProfileUri = selectedImageUri
                    binding.profile.setImageURI(selectedImageUri)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val bitmap = ImageConversionHelper.loadBitmapFromUri(
                            this@GroupProfilePageActivity,
                            selectedImageUri
                        )
                        bitmap?.let { groupViewModel.addProfile(groupId, it) }
                    }
                }
            }
            groupViewModel.isBottomSheetOpen = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in_center, R.anim.dont_slide)
        binding = GroupProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        binding.profile.setOnClickListener {
            showEnlargedImage(binding.profile.drawable)
        }

        binding.editIcon.setOnClickListener {
            if (!groupViewModel.isBottomSheetOpen) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityResultForProfilePhoto.launch(intent)
                groupViewModel.isBottomSheetOpen = true
            }
        }
    }

    private fun showEnlargedImage(imageDrawable: Drawable?) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_enlarged_image)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        val enlargedImageView = dialog.findViewById<ImageView>(R.id.enlarged_image)
        val closeImageView = dialog.findViewById<ShapeableImageView>(R.id.close)
        closeImageView.setOnClickListener {
            dialog.dismiss()
        }
        imageDrawable?.let {
            enlargedImageView.setImageDrawable(it)
        }
        dialog.show()
    }

    private fun setObserver() {
        userViewModel.userData.observe(this) { users ->
            members = users
            membersId = users.map { it.userId }

            binding.groupMemberTitle.text = "Group members (${membersId.size})"
            if (users.isNullOrEmpty()) {
                Toast.makeText(this, "membersNotFound", Toast.LENGTH_SHORT).show()
            } else {
                adapter.updateData(users)
            }
        }

        supportFragmentManager.setFragmentResultListener("addFriend", this) { _, _ ->
            showLoading()
            groupViewModel.addMembers(getLoggedUserId(), groupId, viewModel.selectedList.map {
                it.userId
            })
        }

        groupViewModel.addMembersResponse.observe(this) { response ->
            when (response) {
                is PresentationLayerResponse.Success -> {
                    val list = membersId.toMutableList()
                    viewModel.selectedList.forEach { user ->
                        list.add(user.userId)
                    }
                    membersId = list
                    viewModel.selectedList = mutableListOf()
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
        createdBy = intent.getLongExtra("createdBy", -1)
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
        adapter = UserListingAdapter(this, object : UserListingAdapter.Callback {
            override suspend fun getImage(userId: Long): Bitmap? {
                return viewModel.getProfile(userId)
            }

            override fun clickListener(user: UserProfileSummary) {
                if (user.userId != getLoggedUserId()) {
                    val intent =
                        Intent(this@GroupProfilePageActivity, FriendProfilePageActivity::class.java)
                    intent.putExtra("userId", getLoggedUserId())
                    intent.putExtra("friendId", user.userId)
                    intent.putExtra("friendName", user.name)
                    intent.putExtra("friendNumber", user.phone)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@GroupProfilePageActivity, ProfileActivity::class.java)
                    intent.putExtra("userId", getLoggedUserId())
                    intent.putExtra("name", user.name)
                    intent.putExtra("phone", user.phone)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.dont_slide)
                }
            }
        })
        adapter.setGroupAdmin(createdBy)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
    }

    private fun getLoggedUserId(): Long {
        if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            return preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
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
        setResult(Activity.RESULT_OK, intent.putExtra("membersId", membersId.toLongArray()))
        super.finish()
    }
}
