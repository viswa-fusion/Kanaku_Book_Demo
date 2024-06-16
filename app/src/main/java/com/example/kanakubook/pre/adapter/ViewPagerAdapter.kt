package com.example.kanakubook.pre.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.kanakubook.pre.fragment.FriendsFragment
import com.example.kanakubook.pre.fragment.GroupFragment
import com.example.kanakubook.util.Constants

class ViewPagerAdapter(fragment: Fragment, private val layOutTag: String = Constants.FOR_TAB_LAYOUT) : FragmentStateAdapter(fragment){

    val f1 = GroupFragment(layOutTag)
    var f2 = FriendsFragment(layOutTag)
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> f1
            1 -> f2
            else -> throw ClassNotFoundException("position missing")
        }
    }
}