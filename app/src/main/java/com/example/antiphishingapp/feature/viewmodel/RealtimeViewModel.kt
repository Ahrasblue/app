package com.example.antiphishingapp.feature.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.example.antiphishingapp.feature.repository.RealtimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import okio.ByteString

class RealtimeViewModel : ViewModel() {

    private val repository = RealtimeRepository()

    private val _latestMessage = MutableStateFlow<RealtimeMessage?>(null)
    val latestMessage: StateFlow<RealtimeMessage?> = _latestMessage

    fun startSession() {
        repository.connect()

        viewModelScope.launch(Dispatchers.IO) {
            repository.incomingMessages.collectLatest { msg ->
                _latestMessage.value = msg
            }
        }
    }

    fun sendPcmChunk(chunk: ByteString) {
        repository.sendPcm(chunk)
    }

    fun stopSession() {
        repository.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }

    fun clear() {
        _latestMessage.value = null
    }
}
