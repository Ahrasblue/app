package com.example.antiphishingapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.antiphishingapp.feature.repository.AuthRepository
import com.example.antiphishingapp.theme.AntiPhishingAppTheme
import com.example.antiphishingapp.ui.navigation.AppNavGraph
import com.example.antiphishingapp.utils.NotificationHelper
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {

    // AuthRepository 인스턴스 (자동 로그인 체크용)
    private lateinit var authRepository: AuthRepository

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("PERMISSION", "권한 허용됨")
            } else {
                Toast.makeText(this, "권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AuthRepository 초기화
        authRepository = AuthRepository(applicationContext)

        //OpenCV 초기화
        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV 초기화 성공")
        } else {
            Log.e("OpenCV", "OpenCV 초기화 실패")
        }

        // 문자 관련 권한 요청
        checkSmsPermission()

        // 이미지 권한 요청
        checkImagePermission()

        // 알림 권한 요청 (Android 13+)
        checkNotificationPermission()

        // 알림 채널 생성 (SmsReceiver에서 Notification 사용 가능하게)
        NotificationHelper.createChannel(this)

        setContent {
            //자동 로그인 초기 경로 설정
            val startDestination = remember {
                if (authRepository.isAuthenticated()) {
                    "main" // 토큰이 있으면 바로 메인
                } else {
                    "title" // 토큰이 없으면 타이틀 화면
                }
        }

            AntiPhishingAppTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(navController, startDestination)
                }
            }
        }
    }

    /**
     * 앱이 실행 중일 때 새로운 인텐트(주로 소셜 로그인 콜백 URI)를 수신합니다.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri: Uri? = intent.data

        // 인텐트가 ACTION_VIEW (URI 호출) 타입이고 데이터(URI)를 포함하는지 확인
        if (intent.action == Intent.ACTION_VIEW && uri != null) {
            Log.d("SOCIAL_LOGIN", "Received callback URI: $uri")
            // 콜백 URI를 ViewModel에서 접근할 수 있도록 핸들러에 전달
            SocialLoginCallbackHandler.handleUri(uri)
        }
    }

    private fun checkImagePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("PERMISSION", "이미지 접근 권한 허용됨")
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun checkSmsPermission() {
        val smsPermissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )

        val notGranted = smsPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            // this (Activity Context) 사용 가능
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 101)
        } else {
            Log.d("PERMISSION", "문자 관련 권한 이미 허용됨")
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // this (Activity Context) 사용 가능
                ActivityCompat.requestPermissions(this, arrayOf(permission), 102)
            } else {
                Log.d("PERMISSION", "알림 권한 이미 허용됨")
            }
        }
    }

}

// MainActivity 외부에 임시로 정의하는 콜백 핸들러
// Compose 환경에서 URI를 가져가 처리할 수 있도록 도와주는 브리지 역할
object SocialLoginCallbackHandler {
    private var callbackUri: Uri? = null

    // MainActivity가 URI를 저장할 때 사용
    fun handleUri(uri: Uri) {
        callbackUri = uri
    }

    // TitleScreen(ViewModel)이 URI를 가져갈 때 사용 (가져간 후 바로 삭제)
    fun getAndClearUri(): Uri? {
        val uri = callbackUri
        callbackUri = null
        return uri
    }
}