package com.example.antiphishingapp.feature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Context 사용을 위해 AndroidViewModel 상속
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.antiphishingapp.feature.model.LoginRequest
import com.example.antiphishingapp.feature.model.TokenResponse
import com.example.antiphishingapp.feature.repository.AuthRepository // AuthRepository 사용
import com.example.antiphishingapp.network.ApiClient
import kotlinx.coroutines.launch

/**
 * 로그인 화면의 상태 관리 및 로그인 비즈니스 로직을 처리합니다.
 * AndroidViewModel을 상속받아 안전하게 Application Context에 접근합니다.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // AuthRepository 초기화 시 Application Context를 전달
    private val authRepository = AuthRepository(application.applicationContext)

    // UI 상태 LiveData (이메일, 비밀번호 입력 필드)
    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    // UI 상태 LiveData (로딩 및 오류 메시지)
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // 로그인 성공 이벤트 (UI에서 관찰)
    private val _loginSuccess = MutableLiveData(false)
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    // 자동 로그인 상태 LiveData
    private val _isAutoLoginChecked = MutableLiveData(false)
    val isAutoLoginChecked: LiveData<Boolean> = _isAutoLoginChecked

    // UI 이벤트 핸들러
    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }
    fun onErrorShown() { _error.value = null } // 오류 메시지 소비 후 초기화

    // 자동 로그인 체크박스 핸들러
    fun onAutoLoginCheckedChange(isChecked: Boolean) {
        _isAutoLoginChecked.value = isChecked
    }

    /**
     * "로그인하기" 버튼 클릭 시 호출됩니다.
     */
    fun onLoginClicked() {
        val emailValue = _email.value ?: return
        val passwordValue = _password.value ?: return

        // 1차 입력 유효성 검사
        if (emailValue.isBlank() || passwordValue.isBlank()) {
            _error.value = "이메일과 비밀번호를 모두 입력해주세요."
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val request = LoginRequest(email = emailValue, password = passwordValue)
                val response = ApiClient.apiService.login(request) // API 호출

                if (response.isSuccessful) {
                    val tokenResponse: TokenResponse? = response.body()

                    if (tokenResponse != null) {
                        // 1. 토큰을 AuthRepository에 안전하게 저장
                        authRepository.saveTokens(tokenResponse)

                        // 2. 로그인 성공 이벤트 발생 (UI 트리거)
                        _loginSuccess.value = true
                    } else {
                        _error.value = "로그인 성공, 그러나 서버 응답 바디 없음."
                    }
                } else {
                    // 서버 오류 (401 Unauthorized 등) 처리
                    val errorBody = response.errorBody()?.string()
                    _error.value = "로그인 실패: ${errorBody ?: "이메일 또는 비밀번호를 확인해주세요."}"
                }
            } catch (e: Exception) {
                // 네트워크 오류 처리
                _error.value = "네트워크 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}