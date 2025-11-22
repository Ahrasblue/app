package com.example.antiphishingapp.network

/**
 * 소셜 로그인 및 서버 통신에 필요한 환경 변수들을 정의합니다.
 */
object AppConfig {

    // --- 카카오 설정 ---
    const val KAKAO_CLIENT_ID = "cc9b60d42f687ae216470e85c98822f0"
    const val KAKAO_REDIRECT_URI = "http://localhost:8000/auth/kakao/callback"
    const val KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize"

    // --- 네이버 설정 ---
    const val NAVER_CLIENT_ID = "17faQV8AGa02hywfsUTn"
    const val NAVER_CLIENT_SECRET = "vnimMcgfoL"
    const val NAVER_REDIRECT_URI = "http://localhost:8000/auth/naver/callback"
    const val NAVER_AUTH_URL = "https://nid.naver.com/oauth2.0/authorize"
}