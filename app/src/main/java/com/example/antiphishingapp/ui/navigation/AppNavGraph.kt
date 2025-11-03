package com.example.antiphishingapp.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.ui.analysis.AnalysisScreen
import com.example.antiphishingapp.ui.main.MainScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    var analysisResult by remember { mutableStateOf<AnalysisResponse?>(null) }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // ✅ 1️⃣ 이미지 선택 및 분석 요청 화면
        composable("main") {
            MainScreen(
                navController = navController,
                onAnalysisComplete = { response ->
                    analysisResult = response
                    navController.navigate("analysis") // 결과 화면으로 이동
                }
            )
        }

        // ✅ 2️⃣ 분석 결과 화면
        composable("analysis") {
            analysisResult?.let {
                AnalysisScreen(
                    result = it,
                    onBackToMain = {
                        analysisResult = null
                        navController.navigate("main") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
