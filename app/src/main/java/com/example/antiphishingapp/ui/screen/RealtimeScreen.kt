package com.example.antiphishingapp.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.example.antiphishingapp.feature.realtime.RealtimeCallService
import com.example.antiphishingapp.feature.viewmodel.RealtimeViewModel

@Composable
fun RealtimeScreen(
    viewModel: RealtimeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val message by viewModel.latestMessage.collectAsState()
    var isListening by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📞 실시간 보이스피싱 탐지",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (!isListening) {
                    startRealtimeService(context)
                    viewModel.startSession() // 세션 시작
                } else {
                    stopRealtimeService(context)
                    viewModel.stopSession()  // 세션 종료
                }
                isListening = !isListening
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) Color(0xFFD32F2F) else Color(0xFF1976D2)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isListening) "탐지 중지" else "탐지 시작",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (message != null) {
            RealtimeMessageCard(message!!)
        } else {
            Text(
                text = if (isListening) "서버와 연결 중..." else "탐지 준비 중입니다.",
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun RealtimeMessageCard(msg: RealtimeMessage) {
    val bgColor = when (msg.type) {
        "phishing_alert" -> Color(0xFFFFCDD2)
        "transcription" -> Color(0xFFE3F2FD)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (msg.type) {
                "phishing_alert" -> {
                    Text("⚠️ 위험 감지", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    Text("위험 단어 감지: ${msg.alertMessage ?: "알 수 없음"}")
                }
                "transcription" -> {
                    Text("🗣 인식된 음성", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Text(msg.text ?: "(내용 없음)")
                }
                else -> {
                    Text("📡 ${msg.type}", fontWeight = FontWeight.Bold)
                    Text(msg.text ?: "(메시지 없음)")
                }
            }
        }
    }
}

private fun startRealtimeService(context: Context) {
    val intent = Intent(context, RealtimeCallService::class.java)
    context.startForegroundService(intent)
}

private fun stopRealtimeService(context: Context) {
    val intent = Intent(context, RealtimeCallService::class.java)
    context.stopService(intent)
}
