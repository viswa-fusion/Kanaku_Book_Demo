package com.example.kanakubook.pre.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.kanakubook.R
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kanakubook.databinding.HomeScreenActivityBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: HomeScreenActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigation.setupWithNavController(navController)


    }


    override fun onBackPressed() {
        if(binding.homeScreenSearchView.isShowing){
            binding.homeScreenSearchView.hide()
        }else{
            super.onBackPressed()
        }
    }


}
