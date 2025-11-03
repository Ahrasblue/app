package com.example.antiphishingapp.ui.main

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.antiphishingapp.data.repository.AnalysisRepository
import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.utils.bitmapToMultipart
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

@Composable
fun MainScreen(navController: NavController, onAnalysisComplete: (AnalysisResponse) -> Unit) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                if (inputStream == null) {
                    Toast.makeText(context, "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                    return@let
                }

                val bitmap = BitmapFactory.decodeStream(inputStream)
                val part = bitmapToMultipart(bitmap)
                isLoading = true

                AnalysisRepository.analyzeDocument(part)
                    .enqueue(object : Callback<AnalysisResponse> {
                        override fun onResponse(
                            call: Call<AnalysisResponse>,
                            response: Response<AnalysisResponse>
                        ) {
                            isLoading = false
                            if (response.isSuccessful) {
                                val result = response.body()
                                if (result != null) {
                                    Toast.makeText(context, "분석 완료!", Toast.LENGTH_SHORT).show()
                                    onAnalysisComplete(result)
                                    navController.navigate("analysis")
                                }
                            } else {
                                Toast.makeText(context, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<AnalysisResponse>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "통신 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "이미지 처리 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("이미지 분석 중...")
        } else {
            Text("📸 스미싱 의심 문서를 선택하세요", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("이미지 선택하기")
            }
        }
    }
}
