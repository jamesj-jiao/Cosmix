package com.example.streamline.ui.join

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JoinViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Join a party"
    }
    val text: LiveData<String> = _text
}