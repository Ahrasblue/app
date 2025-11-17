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
    private var isConnected = false

    // 서버의 JSON 메시지 스트림 (웹소켓 → UI)
    private val _incomingMessages = MutableSharedFlow<RealtimeMessage>()
    val incomingMessages: SharedFlow<RealtimeMessage> = _incomingMessages

    /**
     * 🔥 WebSocket 연결
     */
    fun connect(onConnected: (() -> Unit)? = null) {
        if (isConnected) return

        val wsUrl = ApiClient.wsUrl("api/transcribe/ws?sr=16000")
        Log.d("RealtimeRepository", "🌐 WebSocket 연결 시도: $wsUrl")

        val request = Request.Builder().url(wsUrl).build()
        val client = OkHttpClient()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                Log.d("RealtimeRepository", "✅ WebSocket 연결 성공")
                onConnected?.invoke()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("RealtimeRepository", "📩 서버 메시지: $text")
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
     * 🔥 PCM 오디오를 바이너리로 전송
     */
    fun sendPcm(chunk: ByteString) {
        if (!isConnected) return
        webSocket?.send(chunk)
    }

    fun disconnect() {
        webSocket?.close(1000, "User stopped")
        isConnected = false
    }
}