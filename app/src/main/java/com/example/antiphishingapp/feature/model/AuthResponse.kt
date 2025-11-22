package com.example.antiphishingapp.feature.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    val email: String,
    @SerializedName("full_name")
    val fullName: String?,
    val phone: String?,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

//로그인, 토큰 갱신 시 서버 응답 모델 (JWT 토큰 포함)
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("token_type")
    val tokenType: String = "bearer", // 기본값 설정

    @SerializedName("requires_additional_info")
    val requiresAdditionalInfo: Boolean = false // 소셜 로그인 시 추가 정보 입력 필요 여부
)