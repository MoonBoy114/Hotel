package com.example.hotel.screens

import android.widget.Space
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.hotel.MaskVisualTransformation
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
    var emailExists by remember { mutableStateOf(false) } // Флаг для проверки существования email
    var phoneExists by remember { mutableStateOf(false) } // Флаг для проверки существования телефона
    val errorMessage by viewModel.errorMessage.observeAsState()
    val mask = MaskVisualTransformation("+7 (###) ###-##-##")
    var passwordIsVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RivieraOrange)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Регистрация",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { newValue ->
                name = newValue
                nameError = if (newValue.isBlank()) {
                    null
                } else if (newValue.length < 3 || newValue.length > 50) {
                    "Имя должно быть от 3 до 50 символов"
                } else {
                    null
                }
            },
            label = { Text("Имя пользователя") },
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
        Spacer(Modifier.height(10.dp))


        TextField(
            value = email,
            onValueChange = { newValue ->
                email = newValue
                emailError = if (newValue.isBlank()) {
                    null
                } else if (!isValidEmail(newValue)) {
                    "Введите корректный email (например, example@domain.com)"
                } else {
                    null
                }
                // Сбрасываем ошибку существования email при изменении
                emailExists = false
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
            isError = emailError != null || emailExists,
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
        if (emailExists) {
            Text(
                text = "Этот email уже зарегистрирован",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(Modifier.height(10.dp))


        TextField(
            value = phone,
            onValueChange = { newValue ->
                val digits = newValue.text.filter { it.isDigit() }.take(10) // 10 цифр
                phone = newValue.copy(text = digits)
                phoneError = if (digits.isBlank()) {
                    null
                } else if (digits.length != 10) {
                    "Телефон должен содержать 10 цифр (например, +7(XXX)XXX-XX-XX)"
                } else {
                    null
                }
                phoneExists = false
            },
            label = { Text("Телефон") },
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
                errorIndicatorColor = Color.Transparent,
                focusedPlaceholderColor = Color.White,
                unfocusedPlaceholderColor = Color.White
            ),
            placeholder = { Text("+7     -  -  ") }, // Фиксированная маска как placeholder
            modifier = Modifier.fillMaxWidth(),
            isError = phoneError != null || phoneExists,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            visualTransformation = mask,
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
        if (phoneExists) {
            Text(
                text = "Этот номер телефона уже зарегистрирован",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(Modifier.height(10.dp))

        // Поле для пароля
        TextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue
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
            visualTransformation = if (passwordIsVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordIsVisible = !passwordIsVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordIsVisible) R.drawable.open_eye else R.drawable.closed_eye
                        ),
                        contentDescription = if (passwordIsVisible) "Скрыть пароль" else "Показать пароль",
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
                println("Кнопка 'Зарегистрироваться' нажата")
                println(
                    "Условия: name.isNotBlank=${name.isNotBlank()}, email.isNotBlank=${email.isNotBlank()}, " +
                            "phone.text.isNotBlank=${phone.text.isNotBlank()}, password.isNotBlank=${password.isNotBlank()}"
                )
                println("Ошибки: nameError=$nameError, emailError=$emailError, phoneError=$phoneError, passwordError=$passwordError")

                if (nameError == null && emailError == null && phoneError == null && passwordError == null &&
                    name.isNotBlank() && email.isNotBlank() && phone.text.isNotBlank() && password.isNotBlank()
                ) {
                    println("Условие выполнено, начинаем регистрацию")
                    viewModel.viewModelScope.launch {
                        println("Проверка email: $email")
                        val emailResult = viewModel.isEmailTaken(email)
                        emailResult.onSuccess { emailTaken ->
                            println("Email занят: $emailTaken")
                            if (emailTaken) {
                                emailExists = true
                            } else {
                                println("Проверка телефона: +7${phone.text}")
                                val phoneResult = viewModel.isPhoneTaken("+7${phone.text}")
                                phoneResult.onSuccess { phoneTaken ->
                                    println("Телефон занят: $phoneTaken")
                                    if (phoneTaken) {
                                        phoneExists = true
                                    } else {
                                        println("Регистрация пользователя: $name, $email, +7${phone.text}")
                                        viewModel.registerUser(
                                            name,
                                            email,
                                            "+7${phone.text}",
                                            password
                                        )
                                    }
                                }.onFailure { e ->
                                    viewModel.setErrorMessage("Ошибка проверки телефона: ${e.message}")
                                }
                            }
                        }.onFailure { e ->
                            viewModel.setErrorMessage("Ошибка проверки email: ${e.message}")
                        }
                    }
                } else {
                    println("Условие не выполнено, проверяем поля")
                    if (name.isBlank()) {
                        nameError = "Поле имени не может быть пустым"
                        println("Ошибка: Поле имени пустое")
                    }
                    if (email.isBlank()) {
                        emailError = "Поле email не может быть пустым"
                        println("Ошибка: Поле email пустое")
                    }
                    if (phone.text.isBlank()) {
                        phoneError = "Поле телефона не может быть пустым"
                        println("Ошибка: Поле телефона пустое")
                    }
                    if (password.isBlank()) {
                        viewModel.setErrorMessage("Поле пароля не может быть пустым")
                        println("Ошибка: Поле пароля пустое")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9B40)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20),
            enabled = name.isNotBlank() && email.isNotBlank() && phone.text.isNotBlank() && password.isNotBlank() &&
                    nameError == null && emailError == null && phoneError == null && passwordError == null
        ) {
            Text("Зарегистрироваться")
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

