package com.example.antiphishingapp.feature.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiphishingapp.feature.model.SignupRequest
import com.example.antiphishingapp.network.ApiClient
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SignUpViewModel : ViewModel() {

    // UI 상태를 나타내는 LiveData
    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _passwordConfirm = MutableLiveData("")
    val passwordConfirm: LiveData<String> = _passwordConfirm

    private val _name = MutableLiveData("")
    val name: LiveData<String> = _name

    private val _phoneNumber = MutableLiveData("")
    val phoneNumber: LiveData<String> = _phoneNumber

    private val _termsChecked = MutableLiveData(false)
    val termsChecked: LiveData<Boolean> = _termsChecked

    private val _privacyChecked = MutableLiveData(false)
    val privacyChecked: LiveData<Boolean> = _privacyChecked

    // 로딩 상태 및 토스트 메시지를 위한 LiveData
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage


    // UI 이벤트 핸들러 함수
    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPass: String) { _password.value = newPass }
    fun onPasswordConfirmChange(newPassConfirm: String) { _passwordConfirm.value = newPassConfirm }
    fun onNameChange(newName: String) { _name.value = newName }
    fun onPhoneNumberChange(newPhone: String) { _phoneNumber.value = newPhone }
    fun onTermsCheckedChange(isChecked: Boolean) { _termsChecked.value = isChecked }
    fun onPrivacyCheckedChange(isChecked: Boolean) { _privacyChecked.value = isChecked }
    fun onToastMessageShown() { _toastMessage.value = null }


    // "회원가입하기" 버튼 클릭 시 호출될 함수
    fun onSignUpClicked() {
        if (!validateInput()) {
            return
        }

        val signupRequest = SignupRequest(
            email = _email.value ?: "",
            password = _password.value ?: "",
            full_name = _name.value ?: "",
            phone = _phoneNumber.value ?: ""
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.signup(signupRequest)
                if (response.isSuccessful) {
                    _toastMessage.value = "회원가입 성공!"
                    // TODO: 회원가입 성공 후 화면 이동 로직
                } else {
                    val errorBody = response.errorBody()?.string() ?: "알 수 없는 오류"
                    _toastMessage.value = "회원가입 실패: $errorBody"
                }
            } catch (e: Exception) {
                _toastMessage.value = "네트워크 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(): Boolean {
        val emailValue = _email.value ?: ""
        val passwordValue = _password.value ?: ""
        val passwordConfirmValue = _passwordConfirm.value ?: ""
        val nameValue = _name.value ?: ""
        val phoneValue = _phoneNumber.value ?: ""
        val termsCheckedValue = _termsChecked.value ?: false
        val privacyCheckedValue = _privacyChecked.value ?: false

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            _toastMessage.value = "올바른 이메일 형식이 아닙니다."
            return false
        }
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#\$%^&+=]).{8,}\$"
        if (!Pattern.matches(passwordPattern, passwordValue)) {
            _toastMessage.value = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
            return false
        }
        if (passwordValue != passwordConfirmValue) {
            _toastMessage.value = "비밀번호가 일치하지 않습니다."
            return false
        }
        if (nameValue.isBlank()) {
            _toastMessage.value = "이름을 입력해주세요."
            return false
        }
        if (phoneValue.isBlank()) {
            _toastMessage.value = "전화번호를 입력해주세요."
            return false
        }
        if (!termsCheckedValue || !privacyCheckedValue) {
            _toastMessage.value = "모든 약관에 동의해주세요."
            return false
        }
        return true
    }
}