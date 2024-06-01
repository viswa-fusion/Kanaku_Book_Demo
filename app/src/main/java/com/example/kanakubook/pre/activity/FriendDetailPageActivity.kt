package com.example.kanakubook.pre.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.kanakubook.databinding.FriendDetailPageActivityBinding

class FriendDetailPageActivity : AppCompatActivity() {

    private lateinit var binding: FriendDetailPageActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FriendDetailPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        binding.name.text = intent.getStringExtra("name")
        val number = "+91 ${intent.getLongExtra("phone", 0)}"
        binding.number.text = number

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