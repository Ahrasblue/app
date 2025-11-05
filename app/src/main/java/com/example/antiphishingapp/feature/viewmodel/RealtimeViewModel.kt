package com.example.antiphishingapp.feature.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.example.antiphishingapp.feature.repository.RealtimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RealtimeViewModel : ViewModel() {
    private val repository = RealtimeRepository()

    private val _latestMessage = MutableStateFlow<RealtimeMessage?>(null)
    val latestMessage: StateFlow<RealtimeMessage?> = _latestMessage

    // 세션 시작
    fun startSession() {
        viewModelScope.launch {
            // 서버와 연결하고 메시지를 처리하는 부분
            repository.connect(
                onMessage = { text ->
                    // 서버 JSON 메시지를 RealtimeMessage로 변환 필요시 파싱
                    _latestMessage.value = RealtimeMessage(type = "transcription", text = text)
                },
                onError = { err ->
                    _latestMessage.value =
                        RealtimeMessage(type = "error", text = "WebSocket 오류: $err")
                }
            )
        }
    }

    // 음성 데이터 전송 (PCM 데이터)
    fun sendChunk(chunk: ByteArray) {
        viewModelScope.launch {
            repository.sendAudioChunk(chunk)
        }
    }

    fun stopSession() {
        repository.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}