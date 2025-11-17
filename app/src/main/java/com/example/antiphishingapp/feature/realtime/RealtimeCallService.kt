package com.example.antiphishingapp.feature.realtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.antiphishingapp.R
import com.example.antiphishingapp.network.ApiClient
import com.example.antiphishingapp.utils.NotificationHelper
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import com.example.antiphishingapp.feature.repository.RealtimeRepository

class RealtimeCallService : Service() {
    private var webSocket: WebSocket? = null
    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private val repository = RealtimeRepository()

    override fun onCreate() {
        super.onCreate()
        Log.d("RealtimeCallService", "🎙 Service Created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createForegroundNotification())
        startRecordingAndStreaming()
        return START_STICKY
    }

    /**
     * ✅ AudioRecord로 PCM16 모노 스트림을 WebSocket 바이너리 전송
     */
    private fun startRecordingAndStreaming() {

        // 🔥 WebSocket 연결
        repository.connect()

        // 🎙 오디오 설정
        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()

        // 🔥 PCM 오디오를 WebSocket 바이너리 전송
        recordJob = CoroutineScope(Dispatchers.IO).launch {
            val pcmBuffer = ByteArray(bufferSize)
            while (isActive) {
                val bytesRead = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (bytesRead > 0) {
                    val chunk = pcmBuffer.toByteString(0, bytesRead)
                    repository.sendPcm(chunk)
                }
            }
        }
    }

    /**
     * 🔔 Foreground 서비스 알림
     */
    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, "realtime_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("실시간 보이스피싱 탐지 중")
            .setContentText("통화 내용을 분석하고 있습니다...")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    /**
     * 🔔 알림 채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "realtime_channel",
                "실시간 보이스피싱 탐지",
                NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            audioRecord?.stop()
            audioRecord?.release()
            recordJob?.cancel()
            webSocket?.close(1000, "통화 종료")
        } catch (e: Exception) {
            Log.e("RealtimeCallService", "🧹 리소스 해제 중 오류: ${e.message}")
        }
        Log.d("RealtimeCallService", "🛑 서비스 종료 및 리소스 해제 완료")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
