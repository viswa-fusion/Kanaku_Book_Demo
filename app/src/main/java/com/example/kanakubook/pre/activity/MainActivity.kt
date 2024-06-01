package com.example.kanakubook.pre.activity


import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.kanakubook.R
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kanakubook.databinding.HomeScreenActivityBinding
import com.example.kanakubook.pre.viewmodel.FabViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: HomeScreenActivityBinding
    private val fabViewModel: FabViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        window.navigationBarColor = this.getColor(R.color.black)

//        binding.imageProfile.setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in_right,R.anim.dont_slide)
//        }
//        val searchBar = binding.searchBar
//        binding.homeScreenSearchView.setupWithSearchBar(searchBar)


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
