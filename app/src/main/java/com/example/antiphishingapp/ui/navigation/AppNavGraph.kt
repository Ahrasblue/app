package com.example.antiphishingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.feature.viewmodel.AnalysisViewModel
import com.example.antiphishingapp.feature.viewmodel.LoginViewModel
import com.example.antiphishingapp.ui.screen.FileUploadScreen
import com.example.antiphishingapp.ui.screen.ImageUploadResultScreen
import com.example.antiphishingapp.ui.screen.RealtimeScreen
import com.example.antiphishingapp.ui.main.MainScreen
import com.example.antiphishingapp.ui.screen.DetectHistoryScreen
import com.example.antiphishingapp.ui.screen.SignUpScreen
import com.example.antiphishingapp.ui.screen.TitleScreen
import com.example.antiphishingapp.ui.screen.LoginScreen
import com.example.antiphishingapp.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(navController: NavHostController, startRoute: String) {

    val authViewModel: AuthViewModel = viewModel()
    val analysisViewModel: AnalysisViewModel = viewModel()

    // 🔹 이미지 분석 결과를 보관하는 상태
    val imageUploadResult = remember { mutableStateOf<AnalysisResponse?>(null) }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        // 타이틀 화면
        composable("title") {
            TitleScreen(navController = navController)
        }

        // 로그인 화면
        composable("login") {
            LoginScreen(
                navController = navController,
                viewModel = viewModel<LoginViewModel>()
            )
        }

        // 메인 화면
        composable("main") {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                onAnalysisComplete = { result ->
                    imageUploadResult.value = result
                    navController.navigate("analysis")
                }
            )
        }

        // 파일 업로드 화면 (여기서 분석 요청)
        composable("fileUpload") {
            FileUploadScreen(
                navController = navController,
                authViewModel = authViewModel,
                analysisViewModel = analysisViewModel,

                // 🔹 업로드 성공 시 네비게이션 + 상태 저장
                onUploadSuccess = { result ->
                    imageUploadResult.value = result
                    navController.navigate("imageUploadResult")
                }
            )
        }

        // 탐지 기록 화면
        composable("detectHistory") {
            DetectHistoryScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // 회원가입 화면
        composable("signup") {
            SignUpScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }

        // 🔹 이미지 업로드 결과 화면 (현재 사용)
        composable("imageUploadResult") {
            imageUploadResult.value?.let { result ->
                ImageUploadResultScreen(
                    navController = navController,
                    analysis = result
                )
            }
        }

        // 실시간 통화 화면
        composable("realtime") {
            RealtimeScreen()
        }
    }
}
