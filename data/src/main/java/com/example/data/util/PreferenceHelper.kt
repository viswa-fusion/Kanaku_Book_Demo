package com.example.data.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceHelper(private val context: Context) {


    private var sharedPreferences: SharedPreferences? = null


    private fun sharedPreference(): SharedPreferences? {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("login_pref", Context.MODE_PRIVATE)
        }
        return sharedPreferences
    }

    fun writeLongToPreference(key: String?, value: Long) {
        sharedPreference()
        sharedPreferences!!.edit {
            putLong(key, value)
        }
    }

    fun writeBooleanToPreference(key: String?, value: Boolean) {
        sharedPreference()
        sharedPreferences!!.edit {
            putBoolean(key, value)
        }
    }

    fun readBooleanFromPreference(key: String?): Boolean {
        sharedPreference()
        return sharedPreferences!!.getBoolean(key, false)
    }

    fun writeStringToPreference(key: String?, value: String) {
        sharedPreference()
        sharedPreferences!!.edit {
            putString(key, value)
        }
    }

    fun readStringFromPreference(key: String?): String {
        sharedPreference()
        return sharedPreferences!!.getString(key, "").toString()
    }

    fun readLongFromPreference(key: String?): Long {
        sharedPreference()
        return sharedPreferences!!.getLong(key, -1)
    }


}
