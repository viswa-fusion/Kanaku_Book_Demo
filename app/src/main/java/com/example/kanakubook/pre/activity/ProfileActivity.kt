package com.example.kanakubook.pre.activity

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.data.util.PreferenceHelper
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ProfilePageActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication

class ProfileActivity: AppCompatActivity() {

    private lateinit var binding: ProfilePageActivityBinding
    private lateinit var preferenceHelper : PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceHelper = PreferenceHelper(this)
        binding.button.setOnClickListener {
            if(preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)){
                preferenceHelper.writeLongToPreference(KanakuBookApplication.PREF_USER_ID,-1)
                preferenceHelper.writeBooleanToPreference(KanakuBookApplication.PREF_IS_USER_LOGIN,false)
                val intent = Intent(this, AppEntryPoint::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.dont_slide, R.anim.slide_out_right)
    }
}