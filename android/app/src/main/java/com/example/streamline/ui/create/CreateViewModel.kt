package com.example.streamline.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Create a new party"
    }
    val text: LiveData<String> = _text
}