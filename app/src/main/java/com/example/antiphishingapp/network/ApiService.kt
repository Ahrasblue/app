package com.example.antiphishingapp.network

import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.feature.model.SignupRequest
import com.example.antiphishingapp.feature.model.UserResponse
import com.example.antiphishingapp.feature.model.LoginRequest
import com.example.antiphishingapp.feature.model.TokenResponse

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST



// 문자 분석 요청/응답 모델
data class SmsDetectRequest(
    val sender_hash: String,
    val urls: List<String>,
    val texts: List<String>,
    val received_at: Long
)

data class SmsDetectResponse(
    val phishing_score: Double,
    val keywords_found: List<String>,
    val url_results: Map<String, Map<String, Any>>
)

interface ApiService {

    // ✅ 서버 상태 확인 (GET /healthz)
    @GET("healthz")
    fun checkHealth(): Call<String>

    // ✅ 단일 이미지 업로드 (POST /upload-image)
    @Multipart
    @POST("upload-image")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    // ✅ 여러 이미지 업로드 (POST /upload-images)
    @Multipart
    @POST("upload-images")
    fun uploadMultipleImages(
        @Part files: List<MultipartBody.Part>
    ): Call<ResponseBody>

    // ✅ 음성 파일 업로드 (POST /api/transcribe/upload)
    @Multipart
    @POST("api/transcribe/upload")
    fun uploadAudioFile(
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    // ✅ 음성 변환 상태 조회 (GET /api/transcribe/status/{token})
    @GET("api/transcribe/status/{token}")
    fun getTranscribeStatus(
        @Path("token") token: String
    ): Call<ResponseBody>

    // ✅ 문서 분석 (POST /process-request)
    @Multipart
    @POST("process-request")
    fun processRequest(
        @Part file: MultipartBody.Part
    ): Call<AnalysisResponse> // ✅ 여기만 변경됨

    // ✅ 문자 내용 분석 (POST /api/sms/detect_json)
    @POST("api/sms/detect_json")
    fun detectSmsJson(
        @Body payload: SmsDetectRequest
    ): Call<SmsDetectResponse>

    // ✅ 음성 파일 분석 (STT + 보이스피싱 분석)
    @Multipart
    @POST("api/voice-phishing/analyze-audio")
    fun analyzeAudioFile(
        @Part media: MultipartBody.Part,
        @Part("language") language: RequestBody,
        @Part("analysis_method") method: RequestBody
    ): Call<ResponseBody>


    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<UserResponse>

    // 로그인 (POST /auth/login)
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    // 소셜 로그인 코드를 토큰으로 교환하는 API 함수
    @FormUrlEncoded // application/x-www-form-urlencoded 포맷을 서버가 요구할 수 있으므로 추가
    @POST("auth/kakao/callback")
    suspend fun exchangeKakaoCode(
        @Field("code") code: String
    ): Response<TokenResponse>

    @FormUrlEncoded
    @POST("auth/naver/callback")
    suspend fun exchangeNaverCode(
        @Field("code") code: String,
        @Field("state") state: String
    ): Response<TokenResponse>

}
