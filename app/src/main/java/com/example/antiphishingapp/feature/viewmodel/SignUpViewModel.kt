package com.example.antiphishingapp.feature.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {

    // 각 입력 필드의 텍스트 저장
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

    // 체크박스 체크 여부 저장
    private val _termsChecked = MutableLiveData(false)
    val termsChecked: LiveData<Boolean> = _termsChecked

    private val _privacyChecked = MutableLiveData(false)
    val privacyChecked: LiveData<Boolean> = _privacyChecked


    // UI에서 발생하는 이벤트 처리

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onPasswordConfirmChange(newPasswordConfirm: String) {
        _passwordConfirm.value = newPasswordConfirm
    }

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onPhoneNumberChange(newPhoneNumber: String) {
        _phoneNumber.value = newPhoneNumber
    }

    fun onTermsCheckedChange(isChecked: Boolean) {
        _termsChecked.value = isChecked
    }

    fun onPrivacyCheckedChange(isChecked: Boolean) {
        _privacyChecked.value = isChecked
    }

    fun onSignUpClicked() {
        // TODO: 여기에 실제 회원가입 로직(유효성 검사, 서버 API 호출 등)
    }
}