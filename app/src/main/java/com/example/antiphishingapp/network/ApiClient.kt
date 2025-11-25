package com.example.antiphishingapp.network

import com.example.antiphishingapp.feature.model.SignupRequest
import com.example.antiphishingapp.feature.model.UserResponse

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import retrofit2.Response


object ApiClient {

    // ✅ 서버 기본 주소 (HTTPS)
    const val BASE_URL = "https://antiphishingstt.p-e.kr/"

    // ✅ WebSocket용 주소 자동 변환
    // http → ws, https → wss 로 자동 변경
    val WS_BASE_URL: String
        get() = when {
            BASE_URL.startsWith("https://") -> BASE_URL.replaceFirst("https://", "wss://")
            BASE_URL.startsWith("http://") -> BASE_URL.replaceFirst("http://", "ws://")
            else -> BASE_URL
        }

    // ✅ WebSocket URL Helper
    // 경로(path)를 넣으면 전체 wss 주소를 만들어주는 함수
    fun wsUrl(path: String): String {
        val base = WS_BASE_URL.removeSuffix("/")
        val cleanPath = path.removePrefix("/")
        return "$base/$cleanPath"
    }

    // 🔥 [수정됨] 요청하신 STT 전용 WebSocket URL
    // 결과: "wss://antiphishingstt.p-e.kr/api/transcribe/ws?sr=16000&lang=ko-KR"
    val TRANSCRIPTION_WS_URL: String
        get() = wsUrl("api/transcribe/ws?sr=16000&lang=ko-KR")


    // ✅ OkHttpClient 설정
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ✅ Retrofit 인스턴스 (REST API)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ ApiService 인스턴스
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    suspend fun signup(request: SignupRequest): Response<UserResponse> {
        return apiService.signup(request)
    }
}