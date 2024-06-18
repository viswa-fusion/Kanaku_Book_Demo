package com.example.kanakubook.presentation.viewmodel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FabViewModel : ViewModel() {


    val fabVisibility = MutableLiveData<Boolean>()

    init {
        fabVisibility.value = false
    }

    fun setFabVisibility(visibility: Boolean) {
        fabVisibility.value = (visibility)
        Log.i("test", "visibility: $visibility")
    }
}
