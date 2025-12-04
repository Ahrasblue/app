package com.example.antiphishingapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiphishingapp.R
import com.example.antiphishingapp.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.antiphishingapp.data.local.AppDatabase
import com.example.antiphishingapp.data.local.SmsEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

// ------------------------------------
// ViewModel 설정 (DB 연결용)
// ------------------------------------
class SmsListViewModel(database: AppDatabase) : ViewModel() {
    // DB의 데이터를 실시간으로 감시하는 Flow
    val smsListFlow: Flow<List<SmsEntity>> = database.smsDao().getAllRiskySms()
}

class SmsListViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SmsListViewModel::class.java)) {
            return SmsListViewModel(AppDatabase.getDatabase(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun SmsListScreen() {
    val context = LocalContext.current
    val viewModel: SmsListViewModel = viewModel(factory = SmsListViewModelFactory(context))

    // DB 데이터를 State로 변환 (데이터 변경 시 UI 자동 갱신)
    val smsList by viewModel.smsListFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary100)
            .padding(start = 24.dp, end = 27.dp)
    ) {
        Spacer(modifier = Modifier.height(67.dp))
        SearchBar()
        Spacer(modifier = Modifier.height(20.dp))
        FilterBar()
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(smsList) { smsEntity ->
                SmsCard(sms = smsEntity)
            }
        }
    }
}

@Composable
fun SearchBar() {
    var text by remember { mutableStateOf("") }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        singleLine = true,
        textStyle = AppTypography.bodyLarge.copy(color = Primary800),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .background(
                        color = Primary200,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mag),
                    contentDescription = "Search Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Primary800)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "원하는 내역을 검색하세요.",
                            style = AppTypography.bodyLarge,
                            color = Primary800
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun FilterBar() {
    val filterTextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = R.drawable.filter),
            contentDescription = "Filter Icon",
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(Grayscale900)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "필터",
            style = filterTextStyle,
            color = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최신순",
                style = filterTextStyle,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                painter = painterResource(id = R.drawable.poly),
                contentDescription = "Sort order",
                modifier = Modifier.size(width = 9.dp, height = 6.dp),
                colorFilter = ColorFilter.tint(Color(0xFFD9D9D9))
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun SmsCard(sms: SmsEntity) {
    val cardTextStyle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )

    // 날짜 변환 (Long -> String)
    val dateFormat = SimpleDateFormat("a hh:mm", Locale.US) // 예: PM 10:30
    val timeString = dateFormat.format(Date(sms.receivedDate))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Grayscale100,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.pic01),
            contentDescription = "Message Icon",
            modifier = Modifier.size(32.dp),
            colorFilter = ColorFilter.tint(Primary900)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sms.sender,
                    style = cardTextStyle,
                    color = Grayscale900
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = timeString,
                    style = cardTextStyle,
                    color = Grayscale900,
                    textAlign = TextAlign.Right
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            HighlightedText(
                text = sms.content,
                keywords = sms.keywords,
                style = cardTextStyle.copy(color = Grayscale900)
            )
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    keywords: List<String>,
    style: TextStyle,
) {
    if (keywords.isEmpty()) {
        Text(text = text, style = style)
        return
    }

    val annotatedString = buildAnnotatedString {
        val regexPattern = keywords.joinToString("|") { Regex.escape(it) }
        val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)
        val matches = regex.findAll(text)
        var lastIndex = 0

        for (match in matches) {
            append(text.substring(lastIndex, match.range.first))
            withStyle(style = SpanStyle(color = Primary900)) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    Text(text = annotatedString, style = style)
}

@Preview(showBackground = true)
@Composable
fun SmsListScreenPreview() {
    AntiPhishingAppTheme {
        SmsListScreen()
    }
}