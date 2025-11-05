package com.example.antiphishingapp.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.antiphishingapp.R
import com.example.antiphishingapp.feature.realtime.RealtimeCallService

object NotificationHelper {
    private const val CHANNEL_ID_SMS = "sms_alerts"
    private const val CHANNEL_ID_CALL = "call_detection_channel"

    // ✅ 기존 문자 알림 (그대로 유지)
    fun createChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID_SMS) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID_SMS,
                "SMS 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "의심 문자 탐지 알림 채널"
            nm.createNotificationChannel(channel)
        }
    }

    fun showSmsAlert(
        context: Context,
        title: String,
        body: String,
        id: Int = (1000..9999).random()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        createChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_SMS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    // ✅ 새로 추가: "전화 수신 시 보이스피싱 탐지 시작" 알림
    fun createCallChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID_CALL) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID_CALL,
                "보이스피싱 탐지",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "전화 수신 시 보이스피싱 탐지 알림"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun showPhishingAlert(context: Context) {
        createCallChannel(context)

        val intent = Intent(context, RealtimeCallService::class.java)
        intent.putExtra("SERVER_URL", "wss://your-server.com/ws/transcribe/stream")

        val pendingIntent = PendingIntent.getService(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CALL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("보이스피싱 탐지 준비됨")
            .setContentText("탐지를 시작하려면 눌러주세요.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(2001, builder.build())
    }
}