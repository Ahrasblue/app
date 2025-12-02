package com.example.antiphishingapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.antiphishingapp.R
import com.example.antiphishingapp.theme.Pretendard
import com.example.antiphishingapp.theme.Primary900
import com.example.antiphishingapp.theme.Primary300

@Composable
fun PhoneAlertCard(
    onStartDetect: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFBFF)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    painter = painterResource(R.drawable.ic_call),  // 아이콘 추가 필요
                    contentDescription = null,
                    tint = Primary900,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "전화 수신이 감지되었습니다.",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    color = Primary900
                )
            }

            Spacer(Modifier.height(26.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = onStartDetect,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary900),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("탐지 시작", color = Color.White, fontSize = 14.sp)
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary300),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("알림 지우기", color = Primary900, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhoneAlertCardPreview() {
    PhoneAlertCard(onStartDetect = {}, onDismiss = {})
}