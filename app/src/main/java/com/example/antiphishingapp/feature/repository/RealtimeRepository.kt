package com.example.antiphishingapp.feature.repository

import android.util.Log
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.example.antiphishingapp.network.ApiClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import okio.ByteString

class RealtimeRepository {

    private var webSocket: WebSocket? = null
    private val client = ApiClient.apiService  // Retrofit 인스턴스 사용

    // ✅ 서버로부터 오는 메시지를 스트림으로 내보냄
    private val _incomingMessages = MutableSharedFlow<RealtimeMessage>()
    val incomingMessages: SharedFlow<RealtimeMessage> = _incomingMessages

    private var isConnected = false

    /**
     * 서버로 PCM 데이터를 전송하는 함수
     */
    suspend fun sendAudioChunk(chunk: ByteArray) {
        // 서버로 PCM 데이터 전송하는 Retrofit API 호출
        val requestBody = chunk.toRequestBody("application/octet-stream".toMediaTypeOrNull())

        try {
            val response = client.sendAudioChunk(requestBody)
            if (response.isSuccessful) {
                Log.d("RealtimeRepository", "PCM 데이터 전송 성공")
            } else {
                Log.e("RealtimeRepository", "PCM 데이터 전송 실패")
            }
        } catch (e: Exception) {
            Log.e("RealtimeRepository", "HTTP 전송 오류: ${e.message}")
        }
    }

    /**
     * WebSocket 연결 시작
     */
    fun connect(onConnected: (() -> Unit)? = null) {
        if (isConnected) return

        val wsUrl = ApiClient.wsUrl("ws/transcribe/stream")
        Log.d("RealtimeRepository", "🌐 WebSocket 연결 시도: $wsUrl")

        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                Log.d("RealtimeRepository", "✅ WebSocket 연결 성공")
                onConnected?.invoke()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("RealtimeRepository", "📩 서버 메시지 수신: $text")
                _incomingMessages.tryEmit(RealtimeMessage.fromJson(text))
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("RealtimeRepository", "❌ WebSocket 오류: ${t.message}")
                isConnected = false
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("RealtimeRepository", "🔒 WebSocket 종료 ($code): $reason")
                isConnected = false
            }
        })
    }

    /**
     * WebSocket 종료
     */
    fun disconnect() {
        webSocket?.close(1000, "User stopped recording")
        isConnected = false
        Log.d("RealtimeRepository", "🛑 WebSocket 연결 종료됨")
    }
}
