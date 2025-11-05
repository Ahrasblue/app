package com.example.antiphishingapp.feature.model

data class RealtimeMessage(
    val type: String,               // "transcription", "phishing_alert", "error" 등
    val text: String? = null,       // 인식된 텍스트
    val alertMessage: String? = null, // 위험 경고 메시지
    val confidence: Double? = null   // 신뢰도 등
)
