package com.example.antiphishingapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // ✅ 서버 기본 주소
    const val BASE_URL = "http://13.125.248.51:8000/"

    // ✅ WebSocket용 주소 자동 변환
    // http → ws, https → wss 로 자동 치환
    val WS_BASE_URL: String
        get() = when {
            BASE_URL.startsWith("https://") -> BASE_URL.replaceFirst("https://", "wss://")
            BASE_URL.startsWith("http://") -> BASE_URL.replaceFirst("http://", "ws://")
            else -> BASE_URL
        }

    // ✅ WebSocket URL Helper
    // 예: ApiClient.wsUrl("ws/transcribe/stream") → ws://13.125.248.51:8000/ws/transcribe/stream
    fun wsUrl(path: String): String {
        val base = WS_BASE_URL.removeSuffix("/")
        val cleanPath = path.removePrefix("/")
        return "$base/$cleanPath"
    }

    // ✅ OkHttpClient 설정
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃
            .readTimeout(30, TimeUnit.SECONDS)    // 읽기 타임아웃
            .writeTimeout(30, TimeUnit.SECONDS)   // 쓰기 타임아웃
            .build()
    }

    // ✅ Retrofit 인스턴스 생성
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ✅ ApiService 인스턴스 생성 (앱 어디서든 호출 가능)
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}