package com.example.antiphishingapp.feature.repository

import android.util.Log
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.example.antiphishingapp.network.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import okio.ByteString

class RealtimeRepository {

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null

    // 내부 관리용
    private var isConnected: Boolean = false

    // 외부에서 읽기 전용
    val connected: Boolean
        get() = isConnected

    private var pingJob: Job? = null

    private val gson = Gson()
    private val _incomingMessages = MutableSharedFlow<RealtimeMessage>()
    val incomingMessages = _incomingMessages.asSharedFlow()

    fun connect() {
        if (isConnected) return

        client = ApiClient.sharedClient
        val url = ApiClient.TRANSCRIPTION_WS_URL

        val request = Request.Builder()
            .url(url)
            .header("Origin", "https://antiphishingstt.p-e.kr")
            .build()

        webSocket = client!!.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("RealtimeRepository", "✅ WebSocket connected: $url")
                isConnected = true

                pingJob = CoroutineScope(Dispatchers.IO).launch {
                    while (isActive) {
                        delay(15_000)
                        try {
                            ws.send("ping")
                        } catch (e: Exception) {
                            Log.w("RealtimeRepository", "ping 전송 실패: ${e.message}")
                        }
                    }
                }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val parsed = gson.fromJson(text, RealtimeMessage::class.java)
                    CoroutineScope(Dispatchers.IO).launch {
                        _incomingMessages.emit(parsed)
                    }
                } catch (e: Exception) {
                    Log.w("RealtimeRepository", "⚠️ JSON parse error: ${e.message}, text=$text")
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                Log.d("RealtimeRepository", "📥 바이너리 메시지 수신 (${bytes.size} bytes)")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("RealtimeRepository", "❌ WebSocket error: ${t.message}")
                close()
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.w("RealtimeRepository", "⚠️ Closing (server): $code / $reason")
                close()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w("RealtimeRepository", "⚠️ Closed (final): $code / $reason")
                close()
            }
        })
    }

    fun sendPcm(chunk: ByteString) {
        if (isConnected) {
            try {
                Log.d("RealtimeRepository", "sendPcm size=${chunk.size} connected=$isConnected")
                webSocket?.send(chunk)
            } catch (e: Exception) {
                Log.w("RealtimeRepository", "PCM 전송 실패: ${e.message}")
            }
        } else {
            Log.w("RealtimeRepository", "⚠️ WebSocket not connected, cannot send PCM data")
        }
    }

    fun sendText(message: String) {
        if (isConnected) {
            try {
                webSocket?.send(message)
                Log.d("RealtimeRepository", "📤 전송 (text): $message")
            } catch (e: Exception) {
                Log.w("RealtimeRepository", "텍스트 전송 실패: ${e.message}")
            }
        } else {
            Log.w("RealtimeRepository", "⚠️ WebSocket not connected, cannot send text")
        }
    }

    fun disconnect() = close()

    fun close() {
        try {
            if (!isConnected) {
                pingJob?.cancel()
                client = null
                return
            }

            isConnected = false

            try { pingJob?.cancel() } catch (_: Exception) {}

            try {
                webSocket?.close(1000, "종료")
            } catch (e: Exception) {
                Log.w("RealtimeRepository", "webSocket close 실패: ${e.message}")
            }

            client = null
            webSocket = null
            Log.d("RealtimeRepository", "🟢 WebSocket fully closed and resources released")

        } catch (e: Exception) {
            Log.e("RealtimeRepository", "close 실패: ${e.message}")
        }
    }
}
