package com.example.antiphishingapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.antiphishingapp.R
import com.example.antiphishingapp.theme.*

// 가상의 데이터 클래스 (실제 뷰모델에서 가져올 데이터)
data class SuspiciousItem(
    val description: String,
    val isCritical: Boolean = true
)

@Composable
fun ImageUploadResultScreen(
    navController: NavController,
    forgeryScore: Int = 85, // 테스트용 점수 (0~100)
    suspiciousItems: List<SuspiciousItem> = listOf(
        // 테스트용 데이터
        SuspiciousItem("'긴급', '즉시' 키워드를 공문서에서 사용하지 않습니다."),
        SuspiciousItem("직인의 경계가 명확하지 않습니다."),
        SuspiciousItem("문장 내 레이아웃 위치가 일치하지 않습니다."),
        SuspiciousItem("공공기관 로고의 해상도가 비정상적으로 낮습니다."),
        SuspiciousItem("발신 번호가 공식 기관 번호와 다릅니다."),
        SuspiciousItem("문서의 폰트가 관공서 표준 폰트와 다릅니다."),
        SuspiciousItem("담당자 이름과 부서명이 실제와 일치하지 않습니다."),
        SuspiciousItem("URL 링크가 포함되어 있습니다 (관공서는 문자 링크를 보내지 않습니다)."),
        SuspiciousItem("형광펜 효과 등 비공식적인 강조 표시가 있습니다."),
        SuspiciousItem("문서 발행 번호 형식이 올바르지 않습니다."),
        SuspiciousItem("압박감을 주는 위협적인 단어가 다수 포함되어 있습니다."),
        SuspiciousItem("계좌 이체를 요구하는 문구가 포함되어 있습니다."),
        SuspiciousItem("이메일 주소가 공식 도메인(@korea.kr 등)이 아닙니다."),
        SuspiciousItem("첨부 파일의 확장자가 실행 파일(.exe, .zip)입니다."),
        SuspiciousItem("문법적 오류나 어색한 표현이 다수 발견되었습니다.")
    )
) {
    // 스크롤 상태 관리
    val scrollState = rememberScrollState()

    // 1. 점수에 따른 색상 그라데이션 계산 (보간법 사용)
    // 0점(초록) -> 50점(노랑) -> 100점(빨강)
    val scoreColor = calculateScoreColor(forgeryScore)

    // 위험도에 따른 텍스트 및 설명 결정 로직
    val (resultText, descriptionText) = when (forgeryScore) {
        in 70..100 -> "위조 문서 확률이 높습니다." to "보이스피싱 등 의심 용도로\n위조된 문서일 확률이 높습니다."
        in 45..69 -> "위조 문서 확률이 있습니다." to "위조된 문서일 가능성이 있으니\n주의 깊게 확인해주세요."
        else -> "위조 문서 확률이 낮습니다." to "위조된 문서일 확률이 낮습니다.\n첨부한 사진을 다시 확인해주세요."
    }

    Scaffold(
        containerColor = Primary100
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. 상단 고정 영역 (스크롤 X)
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
                        text = resultText, // 간단한 설명 (화면 공간상 줄바꿈 처리 등은 디자인에 따라 조정)
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Grayscale900,
                            lineHeight = 20.sp
                        )
                    )
                }

                // 상세 설명 (요구사항 로직에 따른 텍스트)
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Grayscale600
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 2. 스크롤 가능한 중간 영역 (weight 1f)
            Column(
                modifier = Modifier
                    .weight(1f) // 남은 공간 모두 차지
                    .verticalScroll(scrollState) // 세로 스크롤 가능
                    .padding(horizontal = 24.dp)
            ) {
                // 이미지 탐지 결과 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // 이미지 영역 높이 (예시)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Grayscale50) // 배경색 (이미지 없을 때)
                ) {
                    // 실제 이미지가 있다면 여기에 Image 컴포저블 배치
                    // Image(...)

                    // "이미지 탐지 결과 살펴보기" 버튼 (중앙 배치)
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)) {
                        InteractiveResultButton(
                            text = "이미지 탐지 결과 살펴보기",
                            onClick = { /* 결과 상세 보기 액션 */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 위조 의심 항목 영역
                Text(
                    text = "위조 의심 항목",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Grayscale600,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 회색 박스 (항목 리스트)
                SuspiciousItemsBox(items = suspiciousItems)

                Spacer(modifier = Modifier.height(32.dp)) // 하단 여백 확보
            }

            // 3. 하단 고정 영역 (스크롤 X)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp), // 상하 패딩
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "다른 문서로 다시 시도해볼까요?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Grayscale500,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.clickable {
                        navController.popBackStack() // 뒤로 가기
                    }
                )
            }
        }
    }
}

@Composable
fun calculateScoreColor(score: Int): Color {
    val startColor = Color(0xFF2AC269) // 0% (Green)
    val midColor = Color(0xFFFFBD2D)   // 50% (Yellow)
    val endColor = Color(0xFFF13842)   // 100% (Red)

    return when {
        score <= 50 -> {
            // 0~50 구간: Green -> Yellow
            val fraction = score / 50f
            lerp(startColor, midColor, fraction)
        }
        else -> {
            // 51~100 구간: Yellow -> Red
            val fraction = (score - 50) / 50f
            lerp(midColor, endColor, fraction)
        }
    }
}

/**
 * 위조 의심 항목을 담는 회색 박스
 * 요구사항에 명시된 패딩 값을 정확히 적용함
 */
@Composable
fun SuspiciousItemsBox(items: List<SuspiciousItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Grayscale50)
            // 맨 위/아래 글자와 박스 간격 패딩 16 (여기서는 컨테이너의 Vertical Padding으로 처리)
            .padding(vertical = 16.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // 글자와 왼쪽 박스 경계 패딩 8 (아이콘 포함하여 시작점 기준)
                    // 글자와 오른쪽 박스 경계 패딩 11
                    .padding(start = 8.dp, end = 11.dp),
                verticalAlignment = Alignment.Top // 텍스트가 길어질 경우 상단 정렬
            ) {
                // 경고 아이콘 (이미지)
                Icon(
                    imageVector = Icons.Outlined.Info, // 경고 느낌표 아이콘
                    contentDescription = null,
                    tint = Color(0xFFF13842), // 붉은색 경고 (GradientA_Start 등 활용 가능)
                    modifier = Modifier.size(20.dp)
                )

                // 글자와 경고 이미지 간 패딩 4
                Spacer(modifier = Modifier.width(4.dp))

                // 의심 항목 텍스트
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Grayscale900,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.weight(1f) // 남은 공간 차지
                )
            }

            // 마지막 항목이 아니라면 글자 간 패딩 20 추가
            if (index < items.size - 1) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // 항목이 없을 경우 메시지 처리 (옵션)
        if (items.isEmpty()) {
            Text(
                text = "탐지된 의심 항목이 없습니다.",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Grayscale500
            )
        }
    }
}

/**
 * 이미지 탐지 결과 살펴보기 버튼
 * 기본: 배경 Primary300 / 글씨 Primary900
 * 오버/터치: 배경 Primary900 / 글씨 Primary100(흰색 계열)
 */
@Composable
fun InteractiveResultButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val isActive = isPressed || isHovered

    val containerColor = if (isActive) Primary900 else Primary300
    val contentColor = if (isActive) Primary100 else Primary900

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth(0.9f) // 이미지 박스 너비의 90%
            .height(52.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = Pretendard
                )
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun ImageUploadResultScreenPreview() {
    ImageUploadResultScreen(rememberNavController())
}