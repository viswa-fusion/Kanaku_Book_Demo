package com.example.kanakubook.pre.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MainScreenFragmentBinding

class ActivityFragment :  BaseHomeFragment(R.layout.main_screen_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createFab.visibility = View.GONE
        binding.boxesContainer.visibility = View.GONE
        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE

    }

}