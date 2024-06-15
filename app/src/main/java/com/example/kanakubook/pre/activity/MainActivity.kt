package com.example.kanakubook.pre.activity


import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kanakubook.R
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.databinding.HomeScreenActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.fragment.ViewPagerFragment
import com.example.kanakubook.pre.viewmodel.FabViewModel
import com.example.kanakubook.pre.viewmodel.LoginViewModel
import com.example.kanakubook.pre.viewmodel.UserViewModel
import com.example.kanakubook.util.Constants

class MainActivity : AppCompatActivity() {

    private lateinit var binding: HomeScreenActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigation.setupWithNavController(navController)

        supportFragmentManager.commit {
            replace(R.id.search_view_fragment_container, ViewPagerFragment(Constants.NORMAL_LAYOUT))
            setReorderingAllowed(true)
        }
//        window.navigationBarColor = this.getColor(R.color.black)

//        binding.imageProfile.setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in_right,R.anim.dont_slide)
//        }
//        val searchBar = binding.searchBar
//        binding.homeScreenSearchView.setupWithSearchBar(searchBar)

    }


    override fun onBackPressed() {
        if(binding.homeScreenSearchView.isShowing){
            binding.homeScreenSearchView.hide()
        }else{
            super.onBackPressed()
        }
    }


}
