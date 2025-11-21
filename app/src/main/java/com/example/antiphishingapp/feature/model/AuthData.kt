package com.example.antiphishingapp.feature.model

data class SignupRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val phone: String
)