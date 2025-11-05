package com.example.antiphishingapp.feature.repository

import com.example.antiphishingapp.network.ApiClient
import com.example.antiphishingapp.utils.audioToMultipart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

/**
 * VoiceRepository
 * ------------------------
 * 1️⃣ 사용자가 업로드한 음성 파일을 Multipart로 변환
 * 2️⃣ Retrofit을 통해 서버(/api/voice-phishing/analyze-audio)에 업로드
 * 3️⃣ 서버 응답(JSON)을 String 형태로 반환
 * ------------------------
 */
class VoiceRepository {

    // Retrofit API 인스턴스
    private val api = ApiClient.apiService

    /**
     * 서버에 음성 파일을 업로드하고 결과를 비동기로 받아옴
     *
     * @param file 업로드할 음성 파일 (MP3, WAV)
     * @param language 인식 언어 (기본값 "ko-KR")
     * @param method 분석 방식 ("immediate", "comprehensive", "hybrid")
     * @param onResult 성공 시 콜백 (서버 응답 JSON String)
     * @param onError 실패 시 콜백 (예외 정보)
     */
    fun uploadVoiceFile(
        file: File,
        language: String = "ko-KR",
        method: String = "hybrid",
        onResult: (String?) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            // 1️⃣ 파일을 MultipartBody.Part로 변환
            val mediaPart = audioToMultipart(file)

            // 2️⃣ 문자열 파라미터를 RequestBody로 변환
            val langPart = language.toRequestBody("text/plain".toMediaTypeOrNull())
            val methodPart = method.toRequestBody("text/plain".toMediaTypeOrNull())

            // 3️⃣ Retrofit API 호출
            api.analyzeAudioFile(mediaPart, langPart, methodPart)
                .enqueue(object : Callback<ResponseBody> {

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            val resultText = response.body()?.string()
                            onResult(resultText)
                        } else {
                            onError(Exception("서버 오류: ${response.code()}"))
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        onError(t)
                    }
                })
        } catch (e: Exception) {
            onError(e)
        }
    }
}
