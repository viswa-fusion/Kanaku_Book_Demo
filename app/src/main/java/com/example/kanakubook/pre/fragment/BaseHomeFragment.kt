package com.example.kanakubook.pre.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MainScreenFragmentBinding
import com.example.kanakubook.pre.activity.AddExpenseActivity
import com.example.kanakubook.pre.activity.ProfileActivity

open class BaseHomeFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected lateinit var binding: MainScreenFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainScreenFragmentBinding.bind(view)

        binding.imageProfile.setOnClickListener {
            val intent = Intent(requireActivity(), ProfileActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.dont_slide)
        }

        binding.createExpense.setOnClickListener {
//            val intent = Intent(requireActivity(),AddExpenseActivity::class.java)
//            startActivity(intent)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val searchBar = binding.searchBar
        val homeScreenSearchView = activity?.findViewById<com.google.android.material.search.SearchView>(R.id.homeScreenSearchView)
        Log.i("layoutTestNew","data :$homeScreenSearchView")
        homeScreenSearchView?.setupWithSearchBar(searchBar)
    }
}