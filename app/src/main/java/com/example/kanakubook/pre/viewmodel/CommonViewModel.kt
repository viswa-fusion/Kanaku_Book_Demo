package com.example.kanakubook.pre.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.domain.model.UserProfileSummary
import com.example.kanakubook.pre.adapter.SplitListAdapter
import com.example.kanakubook.pre.fragment.SplitUserListFragment
import kotlin.math.truncate

class CommonViewModel: ViewModel() {
    val selectSplitWithListener = MutableLiveData<SelectionData>()
    var needToGetSplitWith = false
    var isNextButtonClicked = false
    val fragment: SplitUserListFragment by lazy { SplitUserListFragment() }
    lateinit var membersId: List<Long>
    var members: List<UserProfileSummary>? = null
    var isFragmentAdded = false
    var haveSplitWithData = false
    var id: Long = 0


    var totalAmountSplitUserListFragment = 0.0
    var listUserSplitUserListFragment :List<SplitListAdapter.SplitList>? = null
    class SelectionData(
        val members: List<Long>,
        val id: Long,
        val expenseType: Boolean
    )
}