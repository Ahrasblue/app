package com.example.antiphishingapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.antiphishingapp.network.ApiClient
import com.example.antiphishingapp.network.SmsDetectRequest
import com.example.antiphishingapp.network.SmsDetectResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                var sender: String? = null
                val sb = StringBuilder()
                for (msg in messages) {
                    sender = msg.originatingAddress
                    sb.append(msg.messageBody)
                }
                val rawText = sb.toString().trim()
                Log.d("SmsReceiver", "📩 Received SMS: $sender / ${rawText.take(80)}...")

                // 비동기로 서버 전송
                CoroutineScope(Dispatchers.IO).launch {
                    sendToServer(context!!, sender ?: "unknown", rawText)
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "onReceive error: ${e.message}")
        }
    }

    private fun sendToServer(context: Context, sender: String, rawText: String) {
        try {
            // 1️⃣ 해시 생성
            val salt = SaltKeeper.getSalt(context)
            val senderHash = Sanitizer.sha256Hash(sender, salt)

            // 2️⃣ URL 추출 및 나머지 텍스트 분리
            val urls = Sanitizer.extractUrls(rawText)
            val textOnly = Sanitizer.removeUrls(rawText)
            val texts = Sanitizer.splitToSentences(textOnly)

            // 3️⃣ 요청 모델 구성
            val payload = SmsDetectRequest(
                sender_hash = senderHash,
                urls = urls,
                texts = texts,
                received_at = System.currentTimeMillis()
            )

            // 4️⃣ 서버 전송
            ApiClient.apiService.detectSmsJson(payload).enqueue(object :
                Callback<SmsDetectResponse> {
                override fun onResponse(
                    call: Call<SmsDetectResponse>,
                    response: Response<SmsDetectResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        Log.d(
                            "SmsReceiver",
                            "✅ Phishing=${result?.phishing_score}, keywords=${result?.keywords_found}, urls=${result?.url_results?.size}"
                        )
                    } else {
                        Log.e("SmsReceiver", "❌ Server error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<SmsDetectResponse>, t: Throwable) {
                    Log.e("SmsReceiver", "🚨 Network failure: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("SmsReceiver", "sendToServer error: ${e.message}")
        }
    }
}
