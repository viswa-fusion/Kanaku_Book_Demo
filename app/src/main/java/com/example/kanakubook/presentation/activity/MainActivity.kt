package com.example.kanakubook.presentation.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.example.kanakubook.R
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kanakubook.databinding.HomeScreenActivityBinding
import com.example.kanakubook.presentation.fragment.ViewPagerFragment
import com.example.kanakubook.util.Constants
import com.google.android.material.search.SearchView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: HomeScreenActivityBinding


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigation.setupWithNavController(navController)




    }


    override fun onBackPressed() {
        val view = window.findViewById<SearchView>(R.id.homeScreenSearchView1)
        if(view.isShowing){
            view.hide()
        }else{
            super.onBackPressed()
        }
    }
}
