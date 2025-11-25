package com.example.antiphishingapp.feature.model

data class RealtimeMessage(
    val type: String, // "transcription" or "phishing_alert"
    val text: String? = null,
    val is_final: Boolean? = null,

    val alert_type: String? = null,
    val risk_level: Int? = null,
    val risk_probability: Double? = null,
    val phishing_type: String? = null,
    val keywords: List<String>? = null,

    val is_phishing: Boolean? = null,
    val confidence: Double? = null,
    val analyzed_length: Int? = null
)
