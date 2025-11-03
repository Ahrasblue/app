package com.example.antiphishingapp.data.repository

import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.network.ApiClient
import okhttp3.MultipartBody
import retrofit2.Call

object AnalysisRepository {
    private val api = ApiClient.apiService

    fun analyzeDocument(file: MultipartBody.Part): Call<AnalysisResponse> {
        return api.processRequest(file)
    }
}
