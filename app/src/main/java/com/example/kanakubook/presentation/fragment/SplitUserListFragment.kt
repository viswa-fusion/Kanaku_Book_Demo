package com.example.kanakubook.presentation.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanakubook.R
import com.example.kanakubook.databinding.RecyclerviewLayoutBinding
import com.example.kanakubook.presentation.adapter.SplitListAdapter
import com.example.kanakubook.presentation.viewmodel.CommonViewModel
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel


interface CallbackList {

    fun setList(totalAmount: Double, members: List<SplitListAdapter.SplitList>)
    fun getList(): List<SplitListAdapter.SplitList>

}

class SplitUserListFragment : Fragment(R.layout.recyclerview_layout), CallbackList {

    val submitButtonState = MutableLiveData<Boolean>()
    private lateinit var binding: RecyclerviewLayoutBinding
    private lateinit var adapter: SplitListAdapter
    private val friendsViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private val commonViewModel: CommonViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RecyclerviewLayoutBinding.bind(view)

        binding.recyclerview.itemAnimator = null
        binding.recyclerview.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapter = SplitListAdapter(requireActivity(), object : SplitListAdapter.Callback {
            override suspend fun getProfile(userId: Long): Bitmap? {
                return friendsViewModel.getProfile(userId)
            }

            override fun reEvaluateAmount(listCopy: List<SplitListAdapter.SplitList>) {
                val list = mutableListOf<SplitListAdapter.SplitList>()
                listCopy.forEach {
                    list.add(it.copy())
                }
                var deselectCount = 0
                list.forEach {
                    if (!it.isSelected) deselectCount++
                }
                val divider: Double = (list.size - deselectCount).toDouble()
                val currentSplitAmountPerHead =
                    commonViewModel.totalAmountSplitUserListFragment.div(divider)

                list.forEach {
                    if (it.isSelected) {
                        it.amount = String.format("%.2f", currentSplitAmountPerHead)
                    } else {
                        it.amount = "0.00"
                    }
                }
                commonViewModel.listUserSplitUserListFragment = list
                adapter.updateList(list)
                if (divider != 0.0 && currentSplitAmountPerHead >= 1) {
                    submitButtonState.postValue(true)
                } else {
                    submitButtonState.postValue(false)
                }

            }
        })
        binding.recyclerview.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        if (commonViewModel.listUserSplitUserListFragment != null) {
            setList(
                commonViewModel.totalAmountSplitUserListFragment,
                commonViewModel.listUserSplitUserListFragment!!
            )
        }
    }


    override fun setList(totalAmount: Double, members: List<SplitListAdapter.SplitList>) {
        this.commonViewModel.totalAmountSplitUserListFragment = totalAmount
        commonViewModel.listUserSplitUserListFragment = members
        adapter.updateList(members)
    }

    override fun getList(): List<SplitListAdapter.SplitList> {
        return adapter.getCurrentList()
    }
}