package com.example.antiphishingapp.feature.model

data class SignupRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val phone: String
)

//로그인 요청 데이터 모델
data class LoginRequest(
    val email: String,
    val password: String
)