package com.example.kanakubook.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.data.util.PreferenceHelper
import com.example.kanakubook.R
import com.example.kanakubook.presentation.fragment.LoginScreenFragment
import com.example.kanakubook.databinding.AppEntryPointActivityBinding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.util.DefaultDataInjection
import kotlinx.coroutines.launch
import java.io.File

class AppEntryPoint : AppCompatActivity() {

    private val preferenceHelper = PreferenceHelper(this)
    private lateinit var binding: AppEntryPointActivityBinding
    private val loginScreenFragment: LoginScreenFragment by lazy { LoginScreenFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppEntryPointActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_DEFAULT_DATA_INJECTED)) {
            val profileImageFolder = File(filesDir, "profile_images")
            if (!profileImageFolder.exists()) {
                lifecycleScope.launch {
                    DefaultDataInjection(applicationContext).copyDefaultProfileImages(
                        applicationContext
                    )
                }
            }
//          DefaultDataInjection(applicationContext).addDefault()
            preferenceHelper.writeBooleanToPreference(
                KanakuBookApplication.PREF_DEFAULT_DATA_INJECTED,
                true
            )
        }
        if (savedInstanceState == null) {
            if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, loginScreenFragment)
                    setReorderingAllowed(true)
                }
            }
        }
    }


}