package com.example.antiphishingapp.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.antiphishingapp.R
import com.example.antiphishingapp.feature.viewmodel.SocialLoginViewModel
import kotlinx.coroutines.flow.collectLatest
import com.example.antiphishingapp.theme.Primary900
import com.example.antiphishingapp.theme.Primary300
import com.example.antiphishingapp.theme.Pretendard

@Composable
fun TitleScreen(
    navController: NavController,
    socialViewModel: SocialLoginViewModel = viewModel()
) {

    val context = LocalContext.current // Context 획득

    // 소셜 로그인 시작 함수 (Custom Tabs 사용)
    val startSocialLogin: (String) -> Unit = { url ->
        try {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(context, Uri.parse(url)) // Custom Tabs로 URL 실행
        } catch (e: Exception) {
            // Custom Tabs를 사용할 수 없는 경우 일반 브라우저로 폴백
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
    }

    // 로그인 결과에 따른 화면 이동 처리
    LaunchedEffect(Unit) {
        socialViewModel.loginResult.collectLatest { isSuccess ->
            if (isSuccess) {
                // 로그인 성공 시 메인 화면으로 이동 (MainScreen 경로로 가정)
                navController.navigate("main") {
                    popUpTo("title") { inclusive = true } // 이전 화면 모두 제거
                }
            } else {
                // 로그인 실패 시 토스트 메시지 출력
                Toast.makeText(context, "소셜 로그인에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 상단 Title 영역
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f) // 하단 버튼을 아래로 밀기
                ) {
                    Spacer(modifier = Modifier.height(100.dp)) // 상단 여백 조정

                    Text(
                        text = "Title", // 폰트 파일이 없으므로, Pretendard에 큰 사이즈만 적용
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = Primary900,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Bold // 필기체 느낌을 Bold로 대체
                        ),
                        modifier = Modifier.padding(bottom = 120.dp) // 제목 하단 여백
                    )
                }

                // 하단 버튼 그룹
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    // 일반 로그인 버튼
                    AuthButton(
                        text = "로그인",
                        color = Primary900,
                        contentColor = Color.White,
                        onClick = { navController.navigate("login") }
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // 로그인, 회원가입 버튼 간격

                    // 회원가입 버튼
                    AuthButton(
                        text = "회원가입",
                        color = Primary300,
                        contentColor = Primary900, // 텍스트 색상
                        onClick = { navController.navigate("signup") }
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // 일반 버튼과 소셜 버튼 간의 큰 간격

                    // 카카오 로그인 버튼
                    SocialLoginButton(
                        text = "카카오 로그인",
                        iconRes = R.drawable.ic_kakao_logo,
                        backgroundColor = Color(0xFFFFEB00),
                        contentColor = Color.Black,
                        onClick = {
                            val url = socialViewModel.getKakaoAuthUrl()
                            startSocialLogin(url)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // 소셜 버튼 간격

                    // 네이버 로그인 버튼
                    SocialLoginButton(
                        text = "네이버 로그인",
                        iconRes = R.drawable.ic_naver_logo,
                        backgroundColor = Color(0xFF00BF18),
                        contentColor = Color.White,
                        onClick = {
                            val url = socialViewModel.getNaverAuthUrl()
                            startSocialLogin(url)
                        }
                    )
                }
            }
        }
    )
}

// --- 보조 컴포넌트 수정: AuthButton 분리 및 SocialLoginButton 아이콘 간격 수정 ---

@Composable
fun AuthButton(text: String, color: Color, contentColor: Color, onClick: () -> Unit) {
    // 디자인에 맞게 ButtonDefaults를 사용하여 색상 지정
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = contentColor),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = Pretendard))
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp).padding(end = 8.dp) // 아이콘 크기 및 간격 유지
            )
            Text(text = text, style = MaterialTheme.typography.titleMedium.copy(fontFamily = Pretendard))
        }
    }
}