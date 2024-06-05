package com.example.kanakubook.pre.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kanakubook.R
import com.example.kanakubook.databinding.RecyclerviewLayoutBinding
import com.example.kanakubook.pre.adapter.SplitListAdapter
import com.example.kanakubook.pre.viewmodel.FriendsViewModel


interface CallbackList {

    fun setList(totalAmount: Double, members: List<SplitListAdapter.SplitList>)
    fun getList(): List<SplitListAdapter.SplitList>

}

class SplitUserListFragment : Fragment(R.layout.recyclerview_layout), CallbackList {

    val submitButtonState = MutableLiveData<Boolean>()
    private lateinit var binding: RecyclerviewLayoutBinding
    private lateinit var adapter: SplitListAdapter
    private val friendsViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var totalAmount = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RecyclerviewLayoutBinding.bind(view)

        binding.recyclerview.itemAnimator = null
        binding.recyclerview.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapter = SplitListAdapter(object : SplitListAdapter.Callback {
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
                val divider:Double = (list.size - deselectCount).toDouble()
                val currentSplitAmountPerHead = totalAmount.div(divider)

                list.forEach {
                    if (it.isSelected) {
                        it.amount = String.format("%.2f", currentSplitAmountPerHead)
                    } else {
                        it.amount = "0.00"
                    }
                }
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

    override fun setList(totalAmount: Double, members: List<SplitListAdapter.SplitList>) {
        this.totalAmount = totalAmount
        adapter.updateList(members)
    }

    override fun getList(): List<SplitListAdapter.SplitList> {
        return adapter.getCurrentList()
    }
}