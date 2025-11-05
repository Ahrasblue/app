package com.example.antiphishingapp.feature.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiphishingapp.feature.repository.VoiceRepository
import kotlinx.coroutines.launch
import java.io.File

/**
 * VoiceAnalysisViewModel
 * ------------------------
 * UI → Repository → Server → UI
 * 음성 파일 업로드 요청을 관리하고 결과를 LiveData로 전달한다.
 */
class VoiceAnalysisViewModel : ViewModel() {

    private val repository = VoiceRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _result = MutableLiveData<String>()
    val result: LiveData<String> get() = _result

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun analyzeVoice(file: File) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            repository.uploadVoiceFile(
                file = file,
                onResult = { res ->
                    _loading.postValue(false)
                    _result.postValue(res ?: "결과 없음")
                },
                onError = { err ->
                    _loading.postValue(false)
                    _error.postValue(err.message ?: "알 수 없는 오류 발생")
                }
            )
        }
    }
}
