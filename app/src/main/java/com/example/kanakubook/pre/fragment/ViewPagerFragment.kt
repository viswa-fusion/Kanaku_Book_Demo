package com.example.kanakubook.pre.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ViewPagerLayoutBinding
import com.example.kanakubook.pre.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment(R.layout.view_pager_layout) {
    private lateinit var binding: ViewPagerLayoutBinding
    private lateinit var adapter : ViewPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ViewPagerLayoutBinding.bind(view)
        adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab, position ->
            when(position){
                0 ->{
                    tab.text = "Groups"
                }
                1 ->{
                    tab.text = "Friends"
                }
            }
        }.attach()
    }
}