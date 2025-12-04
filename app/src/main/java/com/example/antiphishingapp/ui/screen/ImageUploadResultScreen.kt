package com.example.antiphishingapp.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.antiphishingapp.R
import com.example.antiphishingapp.feature.model.AnalysisResponse
import com.example.antiphishingapp.feature.model.StampBox
import com.example.antiphishingapp.theme.*


/*****************************************************
 * 메인 화면 - 실제 분석 결과 반영
 *****************************************************/
@Composable
fun ImageUploadResultScreen(
    navController: NavController,
    analysis: AnalysisResponse
) {
    val scrollState = rememberScrollState()

    // 서버 점수: 0~1 → 0~100 변환
    val forgeryScore = (analysis.final_risk * 100).toInt()
    val scoreColor = calculateScoreColor(forgeryScore)

    val (resultText, descriptionText) = when (forgeryScore) {
        in 70..100 -> "위조 문서 확률이 높습니다." to "보이스피싱 등 의심 용도로\n위조된 문서일 가능성이 높습니다."
        in 45..69 -> "위조 문서일 가능성이 있습니다." to "주의 깊게 확인해주세요."
        else -> "위조 문서 가능성이 낮습니다." to "큰 문제는 없어 보입니다."
    }

    // 이미지 박스 보기 모드
    var showBoxes by remember { mutableStateOf(false) }

    // 의심 항목 자동 생성
    val suspiciousItems = generateSuspiciousItems(analysis)

    Scaffold(
        containerColor = Primary100
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            /***********************
             * 1) 상단 위험도
             ***********************/
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "문서 위조 위험도",
                    style = MaterialTheme.typography.titleSmall,
                    color = Grayscale600,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$forgeryScore%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Grayscale900,
                            lineHeight = 20.sp
                        )
                    )
                }

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Grayscale600
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }


            /***********************
             * 2) 이미지 + 버튼 영역
             ***********************/
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Grayscale50)
                ) {

                    // 원본 이미지 표시
                    AsyncImage(
                        model = analysis.url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // 박스 오버레이
                    if (showBoxes) {
                        StampBoxOverlay(
                            boxes = analysis.stamp.boxes
                        )

                        // 닫기 버튼 (상단 우측)
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { showBoxes = false }
                        )
                    }

                    // 하단 버튼
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)) {
                        InteractiveResultButton(
                            text = if (!showBoxes) "이미지 탐지 결과 살펴보기" else "원본 이미지 보기",
                            onClick = { showBoxes = !showBoxes }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                /***********************
                 * 3) 의심 항목 목록
                 ***********************/
                Text(
                    text = "위조 의심 항목",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Grayscale600,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SuspiciousItemsBox(items = suspiciousItems)

                Spacer(modifier = Modifier.height(32.dp))
            }

            /***********************
             * 4) 하단 재시도 버튼
             ***********************/
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "다른 문서로 다시 시도해볼까요?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Grayscale500,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}


/*****************************************************
 * 박스 오버레이 (직인 감지된 위치)
 *****************************************************/
@Composable
fun StampBoxOverlay(boxes: List<StampBox>) {
    Canvas(modifier = Modifier.fillMaxSize()) {

        boxes.forEach { box ->
            drawRect(
                color = Color.Red,
                topLeft = Offset(box.x.toFloat(), box.y.toFloat()),
                size = Size(box.width.toFloat(), box.height.toFloat()),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}


/*****************************************************
 * 색상 보간 (0~100점)
 *****************************************************/
@Composable
fun calculateScoreColor(score: Int): Color {
    val startColor = Color(0xFF2AC269)
    val midColor = Color(0xFFFFBD2D)
    val endColor = Color(0xFFF13842)

    return when {
        score <= 50 -> lerp(startColor, midColor, score / 50f)
        else -> lerp(midColor, endColor, (score - 50) / 50f)
    }
}


/*****************************************************
 * 의심 항목 생성기
 *****************************************************/
fun generateSuspiciousItems(analysis: AnalysisResponse): List<SuspiciousItem> {
    val items = mutableListOf<SuspiciousItem>()

    if (analysis.stamp.count > 0)
        items.add(SuspiciousItem("직인 탐지 영역 ${analysis.stamp.count}개 발견됨."))

    if (!analysis.keyword.error && analysis.keyword.total_score > 0)
        items.add(SuspiciousItem("위험 키워드 감지: 총 점수 ${analysis.keyword.total_score}"))

    if (!analysis.layout.error && analysis.layout.score > 0.3f)
        items.add(SuspiciousItem("문서 레이아웃이 비정상적으로 감지되었습니다."))

    if (items.isEmpty())
        items.add(SuspiciousItem("위조 의심 항목이 없습니다.", isCritical = false))

    return items
}


/*****************************************************
 * SuspiciousItemsBox (기존 구조 유지)
 *****************************************************/
@Composable
fun SuspiciousItemsBox(items: List<SuspiciousItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Grayscale50)
            .padding(vertical = 16.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 11.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFFF13842),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Grayscale900,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (index < items.size - 1) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


/*****************************************************
 * 버튼 (기존 유지)
 *****************************************************/
@Composable
fun InteractiveResultButton(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val isHover by interaction.collectIsHoveredAsState()
    val active = isPressed || isHover

    Button(
        onClick = onClick,
        interactionSource = interaction,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) Primary900 else Primary300,
            contentColor = if (active) Primary100 else Primary900
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}


/*****************************************************
 * SuspiciousItem 데이터 클래스
 *****************************************************/
data class SuspiciousItem(
    val description: String,
    val isCritical: Boolean = true
)
