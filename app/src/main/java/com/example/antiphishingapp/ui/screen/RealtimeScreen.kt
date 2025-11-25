package com.example.antiphishingapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.antiphishingapp.feature.viewmodel.RealtimeViewModel
import com.example.antiphishingapp.feature.model.RealtimeMessage
import com.example.antiphishingapp.ui.components.PhoneAlertCard
import com.example.antiphishingapp.ui.components.MessageAlertCard

@Composable
fun RealtimeScreen(
    viewModel: RealtimeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val latestMessage by viewModel.latestMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startSession()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 1️⃣ 음성 인식 중간/최종 텍스트
        if (latestMessage?.type == "transcription") {
            Text(
                text = latestMessage?.text ?: "",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // 2️⃣ 즉시 탐지 (키워드 기반)
        if (latestMessage?.type == "phishing_alert" &&
            latestMessage?.alert_type == "immediate") {

            RiskAlertCard(
                riskLevel = latestMessage?.risk_level ?: 0,
                probability = (latestMessage?.risk_probability ?: 0.0) * 100,
                onDismiss = { viewModel.clear() }
            )
        }

        // 3️⃣ 종합 탐지 결과
        if (latestMessage?.type == "phishing_alert" &&
            latestMessage?.alert_type == "comprehensive") {

            val confidence = (latestMessage?.confidence ?: 0.0) * 100
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (latestMessage?.is_phishing == true)
                            "🚨 보이스피싱 탐지됨 (${String.format("%.1f", confidence)}%)"
                        else
                            "✅ 안전한 통화로 판단됨 (${String.format("%.1f", confidence)}%)",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
fun RiskAlertCard(
    riskLevel: Int,
    probability: Double,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCEDEE)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "⚠️ 보이스피싱 위험 감지",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Red
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "위험도: $riskLevel\n확률: ${"%.1f".format(probability)}%",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("알림 지우기", color = Color.Red)
            }
        }
    }
}
