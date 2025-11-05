package com.example.antiphishingapp.feature.repository

import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.network.ApiClient
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * AnalysisRepository
 * ------------------------
 * 문서(이미지) 파일을 서버로 업로드하여 분석 요청을 수행한다.
 * 서버 엔드포인트: POST /process-request
 */
class AnalysisRepository {

    private val api = ApiClient.apiService

    fun analyzeDocument(
        file: MultipartBody.Part,
        onResult: (AnalysisResponse?) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            api.processRequest(file).enqueue(object : Callback<AnalysisResponse> {
                override fun onResponse(
                    call: Call<AnalysisResponse>,
                    response: Response<AnalysisResponse>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onError(Exception("서버 오류: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<AnalysisResponse>, t: Throwable) {
                    onError(t)
                }
            })
        } catch (e: Exception) {
            onError(e)
        }
    }
}
