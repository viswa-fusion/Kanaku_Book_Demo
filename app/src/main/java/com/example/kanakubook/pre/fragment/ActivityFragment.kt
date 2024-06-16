package com.example.kanakubook.pre.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.model.ActivityModel
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MainScreenFragmentBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.activity.AppEntryPoint
import com.example.kanakubook.pre.adapter.ActivityListingAdapter
import com.example.kanakubook.pre.viewmodel.ActivityViewModel
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.GroupViewModel
import com.example.kanakubook.util.CustomDividerItemDecoration

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
    }

    override fun onResume() {
        super.onResume()
        activityViewModel.getAllMyActivity(getLoggedUserId())
    }

    private fun setLocalObserver() {
        activityViewModel.activityResponse.observe(viewLifecycleOwner){
            when(it){
                is PresentationLayerResponse.Success -> {
                    if(it.data.isEmpty()){
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    }else{
                        binding.emptyTemplate.emptyTemplate.visibility = View.GONE
                        adapter.updateData(it.data)
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(requireActivity(),"cant get any activity",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setAdapter() {
        adapter = ActivityListingAdapter(object: ActivityListingAdapter.CallBack{
            override suspend fun getUserImage(userId: Long): Bitmap? {
                return friendViewModel.getProfile(userId)
            }

            override suspend fun getGroupImage(groupId: Long): Bitmap? {
                return groupViewModel.getProfile(groupId)
            }

            override fun onClickItemListener(groupData: ActivityModel) {

            }

        })
        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.adapter = adapter
        val dividerDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.divider)
        binding.recyclerview.addItemDecoration(
            CustomDividerItemDecoration(requireActivity(), dividerDrawable, 200,16)
        )
    }

    private fun initialSetUp() {
        binding.createFab.visibility = View.GONE
        binding.boxesContainer.visibility = View.GONE
        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
    }

}