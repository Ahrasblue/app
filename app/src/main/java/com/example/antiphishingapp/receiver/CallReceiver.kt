package com.example.antiphishingapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.antiphishingapp.feature.realtime.RealtimeCallService
import androidx.core.content.ContextCompat

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d("CallReceiver", "📞 통화 연결됨 - 서비스 시작") // log 확인
                val serviceIntent = Intent(context, RealtimeCallService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d("CallReceiver", "📴 통화 종료됨 — 서비스 종료 시도") // log 확인
                val stopIntent = Intent(context, RealtimeCallService::class.java)
                context.stopService(stopIntent)
            }
        }
    }
}
