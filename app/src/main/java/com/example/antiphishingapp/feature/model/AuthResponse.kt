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