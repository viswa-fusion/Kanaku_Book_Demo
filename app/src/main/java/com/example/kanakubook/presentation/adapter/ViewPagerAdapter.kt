package com.example.kanakubook.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.kanakubook.presentation.fragment.FriendsFragment
import com.example.kanakubook.presentation.fragment.GroupFragment

class ViewPagerAdapter(
    fragment: Fragment,
    layOutTag: String
) : FragmentStateAdapter(fragment) {

    val f1: GroupFragment by lazy { GroupFragment().apply {
        this.setWithoutToolbarTrue()
        this.setTag(layOutTag)
    } }
    val f2: FriendsFragment by lazy { FriendsFragment().apply {
        this.setWithoutToolbarTrue()
        this.setTag(layOutTag)
    } }
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> f1
            1 -> f2
            else -> throw ClassNotFoundException("position missing")
        }
    }
}