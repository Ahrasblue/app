package com.example.antiphishingapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.antiphishingapp.theme.*

// 가상의 데이터 클래스 (재사용)
// 만약 문서와 다른 데이터 구조가 필요하다면 별도 클래스를 만들어도 됩니다.
// data class VoiceSuspiciousItem(val description: String)

@Composable
fun VoiceUploadResultScreen(
    navController: NavController,
    riskScore: Int = 70, // 이미지 참고: 70% (빨강 구간)
    suspiciousItems: List<SuspiciousItem> = listOf(
        // 보이스피싱 관련 예시 데이터
        SuspiciousItem("'검찰', '금융감독원' 등을 사칭하는 키워드가 감지되었습니다."),
        SuspiciousItem("개인정보 또는 계좌 비밀번호를 요구하는 대화가 포함되어 있습니다."),
        SuspiciousItem("상대방이 고압적인 태도로 즉각적인 행동을 요구합니다."),
        SuspiciousItem("통화 품질에 인위적인 조작 흔적이 발견되었습니다."),
        SuspiciousItem("해외 발신 번호이거나 인터넷 전화 번호일 가능성이 있습니다."),
        SuspiciousItem("피해자의 불안감을 조성하는 단어가 다수 사용되었습니다."),
        SuspiciousItem("대출 상환, 저금리 대출 전환 등의 유인 문구가 있습니다."),
        SuspiciousItem("특정 애플리케이션 설치를 유도하는 내용이 있습니다.")
    )
) {
    // 스크롤 상태 관리
    val scrollState = rememberScrollState()

    // 1. 점수에 따른 색상 그라데이션 계산 (기존 로직 유지)
    val scoreColor = calculateVoiceScoreColor(riskScore)

    // 2. 위험도에 따른 텍스트 결정 로직 (보이스피싱 맥락으로 수정)
    val (resultText, descriptionText) = when (riskScore) {
        in 70..100 -> "보이스피싱 확률이 높습니다." to "보이스피싱 등 범죄 목적으로\n준비된 내용일 확률이 높습니다."
        in 45..69 -> "보이스피싱 확률이 있습니다." to "보이스피싱일 가능성이 있으니\n주의 깊게 확인해주세요."
        else -> "보이스피싱 확률이 낮습니다." to "보이스피싱일 확률이 낮습니다.\n안심하셔도 좋습니다."
    }

    Scaffold(
        containerColor = Primary100
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // [상단 고정 영역] 보이스피싱 위험도 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // 타이틀 수정: 문서 위조 -> 보이스피싱
                Text(
                    text = "보이스피싱 위험도",
                    style = MaterialTheme.typography.titleSmall,
                    color = Grayscale600,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$riskScore%",
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

            // [중간 스크롤 영역] 이미지 영역 없이 바로 의심 항목 리스트 표시
            Column(
                modifier = Modifier
                    .weight(1f) // 남은 공간 모두 차지
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                // 이미지 박스 제거됨

                // 위조 의심 항목 영역
                Text(
                    text = "위조 의심 항목", // 디자인상 텍스트가 '위조 의심 항목'으로 되어 있어 유지 (필요 시 '의심 항목'으로 변경 가능)
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Grayscale600,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 회색 박스 (항목 리스트) - 기존과 동일한 스타일
                VoiceSuspiciousItemsBox(items = suspiciousItems)

                Spacer(modifier = Modifier.height(32.dp)) // 하단 여백
            }

            // [하단 고정 영역] 다시하기 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // 문구 수정: 문서 -> 녹음 파일
                Text(
                    text = "다른 녹음 파일로 다시 시도해볼까요?",
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

// 색상 계산 로직 (기존과 동일)
@Composable
fun calculateVoiceScoreColor(score: Int): Color {
    val startColor = Color(0xFF2AC269) // 0% (Green)
    val midColor = Color(0xFFFFBD2D)   // 50% (Yellow)
    val endColor = Color(0xFFF13842)   // 100% (Red)

    return when {
        score <= 50 -> {
            val fraction = score / 50f
            lerp(startColor, midColor, fraction)
        }
        else -> {
            val fraction = (score - 50) / 50f
            lerp(midColor, endColor, fraction)
        }
    }
}

// 의심 항목 박스 (기존 ImageUploadResultScreen과 동일한 디자인/패딩)
@Composable
fun VoiceSuspiciousItemsBox(items: List<SuspiciousItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Grayscale50)
            .padding(vertical = 16.dp) // 맨 위/아래 패딩 16dp
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // 왼쪽 패딩 8dp, 오른쪽 패딩 11dp
                    .padding(start = 8.dp, end = 11.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFFF13842),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp)) // 아이콘-글자 간격 4dp

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

            // 항목 간 간격 20dp
            if (index < items.size - 1) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (items.isEmpty()) {
            Text(
                text = "탐지된 의심 항목이 없습니다.",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Grayscale500
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun VoiceUploadResultScreenPreview() {
    VoiceUploadResultScreen(rememberNavController())
}