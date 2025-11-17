package com.example.antiphishingapp.feature.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class RealtimeMessage(
    val kind: String,  // "state", "partial", "final", "risk"
    val t: Double,
    val text: String? = null,

    val immediate: Immediate? = null,
    val comprehensive: Comprehensive? = null
) {
    companion object {
        fun fromJson(json: String): RealtimeMessage {
            return Gson().fromJson(json, RealtimeMessage::class.java)
        }
    }
}

data class Immediate(
    val level: Int,
    val probability: Double,
    @SerializedName("phishing_type")
    val phishingType: String?,
    val keywords: List<String>?,
    val method: String?
)

data class Comprehensive(
    @SerializedName("is_phishing")
    val isPhishing: Boolean,
    val confidence: Double,
    val method: String?,
    @SerializedName("analyzed_length")
    val analyzedLength: Int?
)