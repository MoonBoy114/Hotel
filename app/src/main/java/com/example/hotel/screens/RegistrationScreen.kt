package com.example.hotel.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.hotel.R
import com.example.hotel.data.User
import com.example.hotel.data.viewmodel.HotelViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


val GreatVib = FontFamily(
Font(R.font.greatvibes, FontWeight.Normal)
)

val Monst = FontFamily(
    Font(R.font.montserrat_variablefont_wght, FontWeight.Bold)
)

@Composable
fun RegisterScreen(viewModel: HotelViewModel, navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(TextFieldValue("")) } // Храним только цифры
    var password by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val errorMessage by viewModel.errorMessage.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Регистрация", style = MaterialTheme.typography.headlineMedium)

        // Поле для имени
        OutlinedTextField(
            value = name,
            onValueChange = { newValue ->
                name = newValue
                nameError = if (newValue.isBlank() || newValue.length > 50) {
                    "Имя должно быть от 1 до 50 символов"
                } else {
                    null
                }
            },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        nameError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        // Поле для email
        OutlinedTextField(
            value = email,
            onValueChange = { newValue ->
                email = newValue
                emailError = if (isValidEmail(newValue)) {
                    null
                } else {
                    "Введите корректный email (например, example@domain.com)"
                }
            },
            label = { Text("Email") },
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

        // Поле для телефона с фиксированной маской +7(___)___-__-__
        OutlinedTextField(
            value = phone,
            onValueChange = { newValue ->
                // Извлекаем только цифры из введённого текста
                val digits = newValue.text.filter { it.isDigit() }.take(10) // 10 цифр
                phone = newValue.copy(text = digits)
                phoneError = if (digits.length == 10) {
                    null
                } else {
                    "Телефон должен содержать 10 цифр (например, +7(XXX)XXX-XX-XX)"
                }
            },
            label = { Text("Телефон") },
            placeholder = { Text("+7(___)___-__-__") }, // Фиксированная маска как placeholder
            modifier = Modifier.fillMaxWidth(),
            isError = phoneError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )
        phoneError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        // Поле для пароля
        OutlinedTextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue
                passwordError = if (newValue.length < 6 || newValue.length > 20) {
                    "Пароль должен быть от 6 до 20 символов"
                } else {
                    null
                }
            },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
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
                if (nameError == null && emailError == null && phoneError == null && passwordError == null &&
                    name.isNotBlank() && email.isNotBlank() && phone.text.isNotBlank() && password.isNotBlank()
                ) {
                    // Добавляем префикс +7 только при передаче в viewModel
                    viewModel.registerUser(name, email, "+7${phone.text}", password)
                } else {
                    if (name.isBlank()) nameError = "Поле имени не может быть пустым"
                    if (email.isBlank()) emailError = "Поле email не может быть пустым"
                    if (phone.text.isBlank()) phoneError = "Поле телефона не может быть пустым"
                    if (password.isBlank()) viewModel.setErrorMessage("Поле пароля не может быть пустым")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && email.isNotBlank() && phone.text.isNotBlank() && password.isNotBlank() &&
                    nameError == null && emailError == null && phoneError == null && passwordError == null
        ) {
            Text("Зарегистрироваться")
        }

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Уже есть аккаунт? Войти")
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            LaunchedEffect(it) {
                delay(3000)
                viewModel.clearError()
            }
        }
    }
}

// Функция для валидации email
private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}