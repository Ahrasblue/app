package com.example.antiphishingapp.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.antiphishingapp.R
import com.example.antiphishingapp.theme.AntiPhishingAppTheme
import com.example.antiphishingapp.theme.AppTypography
import com.example.antiphishingapp.theme.Grayscale300
import com.example.antiphishingapp.theme.Grayscale600
import com.example.antiphishingapp.theme.Grayscale700
import com.example.antiphishingapp.theme.Grayscale800
import com.example.antiphishingapp.theme.Grayscale900
import com.example.antiphishingapp.theme.NPSFont
import com.example.antiphishingapp.theme.Primary100
import com.example.antiphishingapp.theme.Primary200
import com.example.antiphishingapp.theme.Primary300
import com.example.antiphishingapp.theme.Primary900

@Composable
fun FileUploadScreen(
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Primary100
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            TopBar()
            Spacer(modifier = Modifier.height(62.dp))

            FileUploadHeader()
            Spacer(modifier = Modifier.height(32.dp))

            ActionCard(
                title = "이미지 업로드",
                description = "의심되는 문서 스캔 이미지를 첨부해\n위험도 확인이 가능합니다.",
                iconRes = R.drawable.image_upload,
                onClick = { /* TODO: 이미지 업로드 로직으로 이동 */ }
            )
            Spacer(modifier = Modifier.height(25.dp))

            ActionCard(
                title = "음성 업로드",
                description = "의심되는 통화 녹음 파일을 첨부해\n위험도 확인이 가능합니다.",
                iconRes = R.drawable.voice_upload,
                onClick = { /* TODO: 음성 파일 업로드 로직으로 이동 */ }
            )

            Spacer(modifier = Modifier.weight(1f))
            HelpSection(modifier = Modifier.padding(vertical = 64.dp))
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.clickable { /* No action */ },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Grayscale300)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "홍길동",
                style = AppTypography.titleMedium,
                color = Grayscale800
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /* No action */ }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun FileUploadHeader() {
    Column {
        Text(
            text = "문서 위조 탐지 및 보이스피싱 탐지",
            style = AppTypography.bodyMedium,
            color = Grayscale900
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Primary900)) {
                    append("파일")
                }
                append("을 ")
                withStyle(style = SpanStyle(color = Primary900)) {
                    append("업로드")
                }
                append("하여")
            },
            style = AppTypography.headlineLarge.copy(
                fontFamily = NPSFont,
                fontWeight = FontWeight.Normal
            ),
            color = Grayscale900
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Primary900)) {
                    append("의심 정황")
                }
                append("을 확인해요.")
            },
            style = AppTypography.headlineLarge.copy(
                fontFamily = NPSFont,
                fontWeight = FontWeight.Normal
            ),
            color = Grayscale900
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardColor = if (isPressed) Primary900 else Primary200
    val titleColor = if (isPressed) Primary300 else Primary900
    val descriptionColor = if (isPressed) Primary300 else Grayscale700

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.headlineLarge.copy(
                        fontFamily = NPSFont,
                        fontWeight = FontWeight.Bold
                    ),
                    color = titleColor
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = description,
                    style = AppTypography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = descriptionColor,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

@Composable
fun HelpSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "도움이 필요하신가요?",
            modifier = Modifier.clickable { /* No action */ },
            style = AppTypography.bodyMedium,
            color = Grayscale600
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FileUploadScreenPreview() {
    AntiPhishingAppTheme {
        FileUploadScreen(
            navController = rememberNavController()
        )
    }
}
