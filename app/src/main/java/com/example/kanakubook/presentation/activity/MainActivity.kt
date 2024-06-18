package com.example.kanakubook.presentation.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
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
    private val fragment: ViewPagerFragment by lazy { ViewPagerFragment(Constants.NORMAL_LAYOUT,true) }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavigation.setupWithNavController(navController)


        supportFragmentManager.commitNow{
            replace(R.id.search_view_fragment_container, fragment)
        }
        val searchView : SearchView = binding.homeScreenSearchView

        searchView.editText.addTextChangedListener {
            filterViewPagerFragments(it.toString())
        }

        searchView.toolbar.setBackgroundColor(getColor(R.color.white))

    }


    private fun filterViewPagerFragments(query: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.search_view_fragment_container)
        if (fragment is ViewPagerFragment) {
            fragment.filterData(query)
        }
    }

    override fun finish() {
        if (binding.homeScreenSearchView.isShowing) {
            binding.homeScreenSearchView.hide()
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.finish()
        }
    }


}
