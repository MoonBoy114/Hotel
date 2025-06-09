package com.example.hotel.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.hotel.R
import com.example.hotel.data.viewmodel.HotelViewModel
import kotlinx.coroutines.delay


val RivieraOrange = Color(0xFFF58D4D)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: HotelViewModel, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val errorMessage by viewModel.errorMessage.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RivieraOrange) // Оранжевый фон
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_of_riviera),
                contentDescription = "Riviera Sunrise Resort & Spa Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = 16.dp)
            )
        }

        TextField(
            value = email,
            onValueChange = { newValue ->
                email = newValue.trim() // Удаляем пробелы
                emailError = if (newValue.isBlank()) {
                    null
                } else if (isValidEmail(newValue)) {
                    null
                } else {
                    "Введите корректный email (например, example@domain.com)"
                }
            },
            label = { Text("Email") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFB470),
                unfocusedContainerColor = Color(0xFFFFB470),
                errorContainerColor = Color(0xFFFFB470),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
                errorLabelColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        emailError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Поле для пароля
        TextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue.trim() // Удаляем пробелы
                passwordError = if (newValue.isBlank()) {
                    null
                } else if (newValue.length < 5 || newValue.length > 20) {
                    "Пароль должен быть от 5 до 20 символов"
                } else {
                    null
                }
            },
            label = { Text("Пароль") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFB470),
                unfocusedContainerColor = Color(0xFFFFB470),
                errorContainerColor = Color(0xFFFFB470),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White,
                errorLabelColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.open_eye else R.drawable.closed_eye
                        ),
                        contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                        tint = Color.White
                    )
                }
            },
            singleLine = true
        )
        passwordError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (emailError == null && passwordError == null &&
                    email.isNotBlank() && password.isNotBlank()
                ) {
                    viewModel.loginUser(email, password)
                } else {
                    if (email.isBlank()) emailError = "Поле email не может быть пустым"
                    if (password.isBlank()) passwordError = "Поле пароля не может быть пустым"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9B40)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20),
            enabled = email.isNotBlank() && password.isNotBlank() &&
                    emailError == null && passwordError == null
        ) {
            Text("Войти")
        }

        TextButton(
            onClick = { navController.navigate("register") },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
        ) {
            Text("Нет аккаунта? Зарегистрироваться")
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            LaunchedEffect(it) {
                delay(3000)
                viewModel.clearError()
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}