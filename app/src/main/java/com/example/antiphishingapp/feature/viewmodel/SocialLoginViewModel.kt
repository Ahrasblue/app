package com.example.antiphishingapp.feature.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiphishingapp.network.AppConfig
import com.example.antiphishingapp.feature.repository.AuthRepository
import com.example.antiphishingapp.ui.SocialLoginCallbackHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.security.SecureRandom
import java.lang.Exception

/**
 * 카카오, 네이버 소셜 로그인 URL 생성 및 인증 흐름을 관리하는 ViewModel.
 * - 인증 URL 생성 (state 저장 포함)
 * - 콜백 URI 처리 및 서버에 토큰 요청
 */
class SocialLoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    // 로그인 성공/실패 여부를 UI에 전달하기 위한 SharedFlow
    private val _loginResult = MutableSharedFlow<Boolean>()
    val loginResult: SharedFlow<Boolean> = _loginResult

    private val NAVER_STATE_KEY = "naver_oauth_state"

    init {
        // 앱이 이미 실행 중인 상태에서 콜백을 받을 경우, 즉시 URI 처리를 시작
        val initialUri = SocialLoginCallbackHandler.getAndClearUri()
        if (initialUri != null) {
            handleCallbackUri(initialUri)
        }
    }

    // --- 카카오 로그인 URL 생성 ---
    fun getKakaoAuthUrl(): String {
        return "${AppConfig.KAKAO_AUTH_URL}?" +
                "client_id=${AppConfig.KAKAO_CLIENT_ID}&" +
                "redirect_uri=${AppConfig.KAKAO_REDIRECT_URI}&" +
                "response_type=code&" +
                "scope=account_email"
    }

    // --- 네이버 로그인 URL 생성 (State 저장 포함) ---
    fun getNaverAuthUrl(): String {
        val state = generateNaverState()
        // state 저장: 콜백 시 검증을 위해 저장소(AuthRepository)에 임시로 저장
        authRepository.saveValue(NAVER_STATE_KEY, state)

        val encodedRedirectUri = URLEncoder.encode(AppConfig.NAVER_REDIRECT_URI, "UTF-8")

        return "${AppConfig.NAVER_AUTH_URL}?" +
                "response_type=code&" +
                "client_id=${AppConfig.NAVER_CLIENT_ID}&" +
                "redirect_uri=$encodedRedirectUri&" +
                "state=$state"
    }

    // 네이버 OAuth2.0의 state 값을 생성 (난수 생성)
    private fun generateNaverState(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    // --- 콜백 URI 처리 ---
    fun handleCallbackUri(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val error = uri.getQueryParameter("error")

        if (error != null || code == null) {
            Log.e("SOCIAL_LOGIN", "Login failed. Code: $code, Error: $error")
            viewModelScope.launch { _loginResult.emit(false) }
            return
        }

        viewModelScope.launch {
            try {
                // Uri Scheme을 통해 제공자 확인 (AndroidManifest.xml의 <data android:scheme>과 일치해야 함)
                when (uri.scheme) {
                    "kakao" -> {
                        // 카카오 로그인 처리
                        authRepository.exchangeCodeForToken("kakao", code, null)
                        _loginResult.emit(true)
                    }
                    // 네이버 리다이렉트 URI의 스키마 (예: antiphishingapp)
                    "antiphishingapp" -> {
                        // 네이버: state 검증 필수
                        val savedState = authRepository.getValue(NAVER_STATE_KEY)
                        authRepository.clearValue(NAVER_STATE_KEY) // 사용 후 state 삭제

                        if (state != null && state == savedState) {
                            authRepository.exchangeCodeForToken("naver", code, state)
                            _loginResult.emit(true)
                        } else {
                            Log.e("SOCIAL_LOGIN", "Naver state mismatch (CSRF warning). State: $state, Saved: $savedState")
                            _loginResult.emit(false)
                        }
                    }
                    else -> {
                        Log.e("SOCIAL_LOGIN", "Unknown scheme: ${uri.scheme}")
                        _loginResult.emit(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("SOCIAL_LOGIN", "API error during token exchange: ${e.message}")
                _loginResult.emit(false)
            }
        }
    }
}