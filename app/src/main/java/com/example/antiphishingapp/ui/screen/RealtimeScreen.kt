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

        if (latestMessage?.kind == "state") {
            PhoneAlertCard(
                onStartDetect = {},
                onDismiss = { viewModel.clear() }
            )
        }

        if (latestMessage?.kind == "partial" || latestMessage?.kind == "final") {
            Text(
                text = latestMessage?.text ?: "",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (latestMessage?.kind == "risk") {
            val immediate = latestMessage?.immediate
            val level = immediate?.level ?: 0
            val probability = immediate?.probability ?: 0.0

            RiskAlertCard(
                riskLevel = level,
                probability = probability,
                onDismiss = { viewModel.clear() }
            )
        }

        if (latestMessage?.kind == "sms_alert") {
            MessageAlertCard(
                onCheckKeyword = {},
                onDismiss = { viewModel.clear() }
            )
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