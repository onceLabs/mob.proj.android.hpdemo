package com.oncelabs.template.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private val _importantMessage: MutableLiveData<String> = MutableLiveData("")
    val importantMessage: LiveData<String> = _importantMessage

    init {
        mutateDummyImportantMessage()
    }

    private fun mutateDummyImportantMessage() {
        val possibleMessages = listOf("FANTASTIC", "NICE", "AMAZING")
        viewModelScope.launch {
            for(i in 0..50) {
                _importantMessage.value = "YOU ARE ${possibleMessages.random()}"
                delay(500)
            }
        }
    }

}