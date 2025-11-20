package com.example.antiphishingapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.antiphishingapp.feature.viewmodel.SignUpViewModel
import com.example.antiphishingapp.theme.AntiPhishingAppTheme
import com.example.antiphishingapp.theme.Grayscale100
import com.example.antiphishingapp.theme.Grayscale800

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel() // ViewModel 주입
) {
    // ViewModel에 저장된 UI 상태들을 관찰하고, 변경될 때마다 화면을 자동으로 다시 그림
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val passwordConfirm by viewModel.passwordConfirm.observeAsState("")
    val name by viewModel.name.observeAsState("")
    val phoneNumber by viewModel.phoneNumber.observeAsState("")
    val termsChecked by viewModel.termsChecked.observeAsState(false)
    val privacyChecked by viewModel.privacyChecked.observeAsState(false)

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            AuthInputField(
                label = "이메일*",
                value = email,
                onValueChange = { viewModel.onEmailChange(it) }, // ViewModel의 함수 호출
                placeholder = "example@email.com"
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                label = "비밀번호*",
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = "특수문자 포함. 8자 이상 입력해주세요",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                label = "비밀번호 확인*",
                value = passwordConfirm,
                onValueChange = { viewModel.onPasswordConfirmChange(it) },
                placeholder = "비밀번호를 다시 입력해주세요",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                label = "이름*",
                value = name,
                onValueChange = { viewModel.onNameChange(it) },
                placeholder = "example@email.com"
            )
            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                label = "전화번호*",
                value = phoneNumber,
                onValueChange = { viewModel.onPhoneNumberChange(it) },
                placeholder = "010-1234-5678",
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(24.dp))

            AgreementCheckBox(
                text = "* 이용약관 동의",
                checked = termsChecked,
                onCheckedChange = { viewModel.onTermsCheckedChange(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AgreementCheckBox(
                text = "* 개인정보 처리방침 동의",
                checked = privacyChecked,
                onCheckedChange = { viewModel.onPrivacyCheckedChange(it) }
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.onSignUpClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "회원가입하기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "이미 계정이 있으신가요?",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { /* TODO: 로그인 화면으로 이동 */ },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun AuthInputField(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String, isPassword: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = Grayscale800
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Grayscale100,
                unfocusedContainerColor = Grayscale100,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Grayscale800
            ),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType)
        )
    }
}

@Composable
private fun AgreementCheckBox(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    ) {
        CustomCheckbox(checked = checked)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(text.substring(0, 1))
                }
                append(text.substring(1))
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CustomCheckbox(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary),
        contentAlignment = Alignment.Center
    ) {

    }
}
