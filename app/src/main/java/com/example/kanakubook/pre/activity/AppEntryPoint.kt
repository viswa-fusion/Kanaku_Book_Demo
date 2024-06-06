package com.example.kanakubook.pre.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.data.util.PreferenceHelper
import com.example.kanakubook.R
import com.example.kanakubook.pre.fragment.LoginScreenFragment
import com.example.kanakubook.databinding.AppEntryPointActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.util.DefaultDataInjection

class AppEntryPoint : AppCompatActivity() {

    private val preferenceHelper = PreferenceHelper(this)
    private lateinit var binding: AppEntryPointActivityBinding
    private val loginScreenFragment : LoginScreenFragment by lazy { LoginScreenFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppEntryPointActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_DEFAULT_DATA_INJECTED)){
            DefaultDataInjection(applicationContext).addDefault()
            preferenceHelper.writeBooleanToPreference(KanakuBookApplication.PREF_DEFAULT_DATA_INJECTED,true)
        }
        if(savedInstanceState == null){
            if(preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)){
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }else {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, loginScreenFragment)
                    setReorderingAllowed(true)
                }
            }
        }
    }
}