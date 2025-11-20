package com.example.antiphishingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.ui.screen.AnalysisScreen
import com.example.antiphishingapp.ui.screen.RealtimeScreen
import com.example.antiphishingapp.ui.main.MainScreen
import com.example.antiphishingapp.ui.screen.SignUpScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    val analysisResult = remember { mutableStateOf<AnalysisResponse?>(null) }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // ✅ 메인 화면 (문서 업로드)
        composable("main") {
            MainScreen(
                navController = navController,
                onAnalysisComplete = { result ->
                    analysisResult.value = result
                }
            )
        }

        // ✅ 회원가입 화면 (새로 추가된 부분)
        composable("signup") {
            SignUpScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }

        // ✅ 문서 분석 결과 화면
        composable("analysis") {
            analysisResult.value?.let { result ->
                AnalysisScreen(
                    result = result,
                    onBackToMain = {
                        navController.popBackStack("main", inclusive = false)
                    }
                )
            }
        }

        // ✅ 실시간 보이스피싱 탐지 화면
        composable("realtime") {
            RealtimeScreen()
        }
    }
}