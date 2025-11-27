package com.example.antiphishingapp.feature.realtime

import android.util.Log
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.google.gson.Gson
import okhttp3.*

class WebSocketClient(
    private val serverUrl: String,
    private val onMessageReceived: (RealtimeMessage) -> Unit
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("WebSocketClient", "WebSocket 연결됨")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WebSocketClient", "📩 수신: $text")   // ← 이 줄 추가
                try {
                    val msg = gson.fromJson(text, RealtimeMessage::class.java)
                    onMessageReceived(msg)
                } catch (e: Exception) {
                    Log.e("WebSocketClient", "메시지 파싱 오류: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketClient", "WebSocket 오류: ${t.message}")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketClient", "WebSocket 종료: $reason")
            }
        })
    }

    fun send(data: String) {
        webSocket?.send(data)
    }

    fun close() {
        webSocket?.close(1000, "Normal closure")
        client.dispatcher.executorService.shutdown()
    }
}
