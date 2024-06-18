package com.example.kanakubook.presentation.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Converters.ActivityType
import com.example.domain.model.ActivityModel
import com.example.domain.model.ExpenseData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.presentation.activity.ExpenseDetailActivity
import com.example.kanakubook.presentation.activity.FriendDetailPageActivity
import com.example.kanakubook.presentation.activity.GroupDetailPageActivity
import com.example.kanakubook.presentation.adapter.ActivityListingAdapter
import com.example.kanakubook.presentation.viewmodel.ActivityViewModel
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.presentation.viewmodel.GroupViewModel
import com.example.kanakubook.util.CustomDividerItemDecoration
import java.util.ArrayList

class ActivityFragment : BaseHomeFragment(R.layout.main_screen_fragment) {

    private lateinit var adapter: ActivityListingAdapter
    private val friendViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private val groupViewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val activityViewModel: ActivityViewModel by viewModels { ActivityViewModel.FACTORY }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetUp()
        setAdapter()
        setLocalObserver()
        showLoading()
    }

    override fun onResume() {
        super.onResume()
        activityViewModel.getAllMyActivity(getLoggedUserId())
    }

    private fun setLocalObserver() {
        activityViewModel.activityResponse.observe(viewLifecycleOwner) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    if (it.data.isEmpty()) {
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    } else {
                        binding.emptyTemplate.emptyTemplate.visibility = View.GONE
                        adapter.updateData(it.data)
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(requireActivity(), "cant get any activity", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            hideLoading()
        }
    }

    private fun setAdapter() {
        adapter = ActivityListingAdapter(object : ActivityListingAdapter.CallBack {
            override suspend fun getUserImage(userId: Long): Bitmap? {
                return friendViewModel.getProfile(userId)
            }

            override suspend fun getGroupImage(groupId: Long): Bitmap? {
                return groupViewModel.getProfile(groupId)
            }

            override fun onClickItemListener(activity: ActivityModel) {
                when (activity.activityType) {
                    ActivityType.ADD_FRIEND -> {
                        val intent = Intent(requireActivity(), FriendDetailPageActivity::class.java)
                        intent.putExtra("name",activity.friend?.name)
                        intent.putExtra("phone",activity.friend?.phone)
                        intent.putExtra("userId",activity.friend?.userId)
                        intent.putExtra("connectionId",activity.connectionId)

                        startActivity(intent)
                    }

                    ActivityType.CREATE_GROUP -> {
                        launchGroup(activity)
                    }

                    ActivityType.ADD_EXPENSE -> {
                        launchGroup(activity)
                    }

                    ActivityType.ADD_MEMBER_TO_GROUP -> {
                        launchGroup(activity)
                    }

                    ActivityType.PAY_FOR_EXPENSE -> {
                        val item :ExpenseData = activity.expense!!

                        val intent =
                            Intent(requireActivity(), ExpenseDetailActivity::class.java)

                        intent.putExtra("userId", getLoggedUserId())
                        intent.putExtra("ownerId", item.spender.userId)
                        intent.putExtra("totalAmount", item.totalAmount)
                        intent.putExtra("ownerName", item.spender.name)
                        intent.putParcelableArrayListExtra("splitList", ArrayList(item.listOfSplits))

                        startActivity(intent)
                    }

                    ActivityType.SPLIT_MEMBER_PAY -> {
                        val item :ExpenseData = activity.expense!!

                        val intent =
                            Intent(requireActivity(), ExpenseDetailActivity::class.java)

                        intent.putExtra("userId", getLoggedUserId())
                        intent.putExtra("ownerId", item.spender.userId)
                        intent.putExtra("totalAmount", item.totalAmount)
                        intent.putExtra("ownerName", item.spender.name)
                        intent.putParcelableArrayListExtra("splitList", ArrayList(item.listOfSplits))

                        startActivity(intent)
                    }
                }
            }

            fun launchGroup(activity: ActivityModel){

                if(activity.group !=null) {
                    groupViewModel.getGroup.removeObservers(viewLifecycleOwner)
                    groupViewModel.getGroup.observe(viewLifecycleOwner){
                        if (it != null){
                            val intent = Intent(requireActivity(), GroupDetailPageActivity::class.java)
                            intent.putExtra("groupName", activity.group?.name)
                            intent.putExtra("groupId", activity.group?.id)
                            intent.putExtra("createdBy", activity.group?.createdBy)
                            val bundle = Bundle()
                            groupViewModel.getGroupByGroupId(activity.group?.id)
                            bundle.putLongArray(
                                "members",
                                it.members.map { it.userId }.toLongArray()
                            )
                            intent.putExtra("bundle", bundle)

                            startActivity(intent)
                        }
                        groupViewModel.getGroup.removeObservers(viewLifecycleOwner)
                    }
                    groupViewModel.getGroupByGroupId(activity.group?.id)

                }else{

                    val intent = Intent(requireActivity(),FriendDetailPageActivity::class.java)
                    intent.putExtra("name",activity.friend?.name)
                    intent.putExtra("phone",activity.friend?.phone)
                    intent.putExtra("userId",activity.friend?.userId)
                    intent.putExtra("connectionId",activity.connectionId)
                    Log.i("intentData","data: ${intent.extras}")
                    startActivity(intent)
                }
            }

        })
        binding.recyclerview.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.adapter = adapter
        val dividerDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.divider)
        binding.recyclerview.addItemDecoration(
            CustomDividerItemDecoration(requireActivity(), dividerDrawable, 200, 16)
        )
    }

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
    }

    private fun initialSetUp() {
        binding.emptyTemplate.content?.text = "You have no activitys at this \n moment"
        binding.createFab.visibility = View.GONE
        binding.boxesContainer.visibility = View.GONE
    }

}