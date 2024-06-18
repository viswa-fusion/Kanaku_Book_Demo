package com.example.kanakubook.presentation.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.model.CommonGroupWIthAmountData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.GroupProfilePageBinding
import com.example.kanakubook.presentation.adapter.CommonGroupListAdapter
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.presentation.viewmodel.GroupViewModel
import com.example.kanakubook.util.CustomDividerItemDecoration
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendProfilePageActivity : AppCompatActivity() {

    private lateinit var binding: GroupProfilePageBinding
    private lateinit var friendName: String
    private lateinit var friendNumber: String
    private var friendId: Long = -1L
    private var loginUserId: Long = -1L
    private val friendViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private val groupViewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private lateinit var adapter: CommonGroupListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in_center, R.anim.dont_slide)
        initialSetUp()
        setListener()
        setAdapter()
        setObserver()

    }

    private fun setAdapter() {
        adapter = CommonGroupListAdapter(this, object : CommonGroupListAdapter.CallBack {
            override suspend fun getImage(groupId: Long): Bitmap? {
                return groupViewModel.getProfile(groupId)
            }

            override fun onClickItemListener(groupData: CommonGroupWIthAmountData) {
                val intent =
                    Intent(this@FriendProfilePageActivity, GroupDetailPageActivity::class.java)
                intent.putExtra("groupName", groupData.group.name)
                intent.putExtra("groupId", groupData.group.id)
                intent.putExtra("createdBy", groupData.group.createdBy)
                val bundle = Bundle()
                bundle.putLongArray("members", groupData.members.map { it.userId }.toLongArray())
                intent.putExtra("bundle", bundle)
                startActivity(intent)
            }

        })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        val dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider)
        binding.recyclerView.addItemDecoration(
            CustomDividerItemDecoration(this, dividerDrawable, 200, 16)
        )
    }

    override fun onResume() {
        super.onResume()
        showLoading()
        friendViewModel.getCommonGroupWithFriendIdWithCalculatedAmount(loginUserId, friendId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("TAG", "")
    }

    private fun setObserver() {
//        Toast.makeText(this,"${friendViewModel.commonGroupResponse.hasActiveObservers()}",Toast.LENGTH_SHORT).show()
        friendViewModel.commonGroupResponse.observe(this) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    if (it.data.isNotEmpty()) {
                        binding.notInSameGroup.emptyTemplate.visibility = View.GONE
                        adapter.updateData(it.data)
                        binding.groupMemberTitle.text = "Common Groups(${it.data.size})"
                    } else {
                        binding.notInSameGroup.emptyTemplate.visibility = View.VISIBLE
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this, "no common groups for this friend", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            hideLoading()
        }
    }

    private fun setListener() {
        binding.close.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.dont_slide, R.anim.fade_out_center)
        }

        binding.profile.setOnClickListener {
            showEnlargedImage(binding.profile.drawable)
        }
    }

    private fun showEnlargedImage(imageDrawable: Drawable?) {
        val dialog = Dialog(this)
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

    private fun getIntentData() {
        loginUserId = intent.getLongExtra("userId", -1)
        friendId = intent.getLongExtra("friendId", -1)
        friendName = intent.getStringExtra("friendName") ?: ""
        friendNumber = intent.getStringExtra("friendNumber") ?: ""
    }

    private fun initialSetUp() {
        binding = GroupProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editIcon.visibility = View.GONE
        binding.addGroup.visibility = View.GONE
        binding.groupMemberTitle.text = "Common Groups(0)"
        getIntentData()
        setIntentData()
    }

    private fun setIntentData() {
        binding.name.text = friendName
        getGroupProfile()
    }

    private fun getGroupProfile() {
        if (friendId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = friendViewModel.getProfile(friendId)
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

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
    }
}