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
    private var isConnected = false
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

                // ping 주기
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
                // 만약 서버가 바이너리 메시지를 보낸다면 처리 (현재는 텍스트 JSON만 사용)
                Log.d("RealtimeRepository", "📥 바이너리 메시지 수신 (${bytes.size} bytes)")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("RealtimeRepository", "❌ WebSocket error: ${t.message}")
                close()
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.w("RealtimeRepository", "⚠️ Closing (server): $code / $reason")
                // 서버가 닫으려는 경우 안전하게 close 처리
                close()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w("RealtimeRepository", "⚠️ Closed (final): $code / $reason")
                // ensure cleanup
                close()
            }
        })
    }

    fun sendPcm(chunk: ByteString) {
        if (isConnected) {
            try {
                webSocket?.send(chunk)
            } catch (e: Exception) {
                Log.w("RealtimeRepository", "PCM 전송 실패: ${e.message}")
            }
        } else {
            Log.w("RealtimeRepository", "⚠️ WebSocket not connected, cannot send PCM data")
        }
    }

    /**
     * 텍스트 프레임 전송 (예: "__END__" 같은 제어 메시지)
     */
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
                // 이미 정리된 상태일 수 있음
                pingJob?.cancel()
                client = null
                return
            }

            isConnected = false
            try {
                pingJob?.cancel()
            } catch (e: Exception) {
                Log.w("RealtimeRepository", "pingJob cancel 실패: ${e.message}")
            }

            try {
                webSocket?.close(1000, "종료")
            } catch (e: Exception) {
                Log.w("RealtimeRepository", "webSocket close 실패: ${e.message}")
            }

            // client 의 executorService 종료는 여기서 하지 않음 (재사용 가능하도록)
            client = null
            webSocket = null
            Log.d("RealtimeRepository", "🟢 WebSocket fully closed and resources released")
        } catch (e: Exception) {
            Log.e("RealtimeRepository", "close 실패: ${e.message}")
        }
    }
}
