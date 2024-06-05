package com.example.kanakubook.pre.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kanakubook.R
import com.example.kanakubook.databinding.DetailPageActivityBinding
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendDetailPageActivity : AppCompatActivity() {

    private lateinit var binding: DetailPageActivityBinding
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
    }

    private fun initialSetUp() {
        binding = DetailPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        supportActionBar?.title = ""

        binding.name.text = intent.getStringExtra("name")
        val number = "+91 ${intent.getLongExtra("phone", 0)}"
        val userId = intent.getLongExtra("userId",-1)
        if(userId != -1L){
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getProfile(userId)?.let {
                    binding.profile.setImageBitmap(it)
                }?:binding.profile.setImageResource(R.drawable.default_profile_image)
            }
        }
        binding.number.text = number
    }

    private fun setListener() {
        binding.createFab.setOnClickListener {
            val intent = Intent(this,AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> {
                false
            }
        }
    }

}