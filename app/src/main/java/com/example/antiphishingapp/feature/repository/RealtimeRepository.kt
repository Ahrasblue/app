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
import java.util.concurrent.TimeUnit

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

        client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val url = ApiClient.wsUrl("api/transcribe/ws_echo")
        val request = Request.Builder().url(url).build()

        webSocket = client!!.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("RealtimeRepository", "✅ WebSocket connected: $url")
                isConnected = true

                pingJob = CoroutineScope(Dispatchers.IO).launch {
                    while (isActive) {
                        delay(15000)
                        ws.send("ping")
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

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("RealtimeRepository", "❌ WebSocket error: ${t.message}")
                close()
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.w("RealtimeRepository", "⚠️ Closing: $code / $reason")
                close()
            }
        })
    }

    fun sendPcm(chunk: ByteString) {
        if (isConnected) {
            webSocket?.send(chunk)
        }
    }

    fun disconnect() = close()

    fun close() {
        try {
            isConnected = false
            pingJob?.cancel()
            webSocket?.close(1000, "종료")
            client?.dispatcher?.executorService?.shutdown()
        } catch (e: Exception) {
            Log.e("RealtimeRepository", "close 실패: ${e.message}")
        }
    }
}
