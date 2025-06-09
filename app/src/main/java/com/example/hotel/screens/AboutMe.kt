package com.example.hotel.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R
import com.example.hotel.data.viewmodel.HotelViewModel
import kotlinx.coroutines.launch

@Composable
fun AboutMe(viewModel: HotelViewModel, navController: NavHostController) {
    val currentUser by viewModel.currentUser.observeAsState()

    // Локальные состояния для редактирования данных
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var phone by remember { mutableStateOf(
        TextFieldValue(viewModel.formatPhoneNumber(currentUser?.phone ?: ""))
    ) }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }

    // Исходные значения для сравнения
    val initialName = currentUser?.name ?: ""
    val initialPhone = viewModel.formatPhoneNumber(currentUser?.phone ?: "")
    val initialEmail = currentUser?.email ?: ""

    // Функция для форматирования номера телефона
    fun formatPhoneNumber(input: String): String {
        // Удаляем всё, кроме цифр
        val digits = input.filter { it.isDigit() }
        // Начинаем с "+"
        val builder = StringBuilder("+")
        // Добавляем цифры с форматированием
        for (i in digits.indices) {
            when (i) {
                0 -> builder.append(digits[i]) // Первая цифра после "+"
                1 -> builder.append(" (").append(digits[i])
                4 -> builder.append(") ").append(digits[i])
                7, 9 -> builder.append("-").append(digits[i])
                else -> builder.append(digits[i])
            }
            // Ограничиваем длину до 11 цифр
            if (i >= 10) break
        }
        return builder.toString()
    }

    // Функция для вычисления новой позиции курсора
    fun calculateCursorPosition(formatted: String, digitsCount: Int): TextRange {
        // Позиция курсора зависит от количества введённых цифр
        var cursorPos = 1 // Начинаем после "+"
        for (i in 0 until digitsCount) {
            when (i) {
                0 -> cursorPos += 1 // После первой цифры: "+7"
                1 -> cursorPos += 3 // После второй цифры: "+7 (9"
                4 -> cursorPos += 4 // После пятой цифры: "+7 (999) "
                7 -> cursorPos += 2 // После восьмой цифры: "+7 (999) 123-"
                9 -> cursorPos += 2 // После десятой цифры: "+7 (999) 123-45-"
                else -> cursorPos += 1
            }
        }
        return TextRange(cursorPos.coerceAtMost(formatted.length))
    }

    // Валидация полей
    val isNameEmpty by remember(name) { mutableStateOf(name.trim().isEmpty()) }
    val isNameTooShort by remember(name) { mutableStateOf(name.trim().length <= 2) }
    val isNameInvalidStart by remember(name) {
        mutableStateOf(
            name.firstOrNull()?.let { firstChar ->
                !firstChar.isLetter() || !firstChar.toString().matches(Regex("[A-Za-zА-Яа-я]"))
            } ?: true // Если имя пустое, считаем, что начало некорректно
        )
    }
    val isNameValid by remember(name) {
        mutableStateOf(!isNameEmpty && !isNameTooShort && !isNameInvalidStart)
    }
    val isPhoneValid by remember(phone) {
        mutableStateOf(viewModel.cleanPhoneNumber(phone.text).length >= 11)
    }
    val isEmailValid by remember(email) {
        mutableStateOf(email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }

    // Проверяем, были ли изменения и валидны ли данные
    val hasChanges by remember(name, phone, email) {
        mutableStateOf(
            (name != initialName || phone.text != initialPhone || email != initialEmail) &&
                    isNameValid && isPhoneValid && isEmailValid
        )
    }

    // Состояние для диалога подтверждения выхода
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Для отображения Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_arrow),
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { navController.popBackStack() },
                            tint = Color.White
                        )
                        Text(
                            text = "Мои данные",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp),
                        containerColor = Color(0xFFF58D4D),
                        shape = RoundedCornerShape(10.dp),
                        content = {
                            Text(
                                text = data.visuals.message,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 38.dp)
            .background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                // Иконка пользователя
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(100.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_profile),
                        contentDescription = "profile icon",
                        tint = Color(0xFFF58D4D)
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Поля для данных пользователя
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    // Имя
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Имя пользователя",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                                BasicTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .padding(top = 4.dp),
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    singleLine = true
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.right_arrow),
                                contentDescription = "Edit",
                                tint = Color(0xFFF58D4D),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        // Отображение ошибки в зависимости от причины
                        when {
                            isNameEmpty -> {
                                Text(
                                    text = "Имя не может быть пустым",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            isNameTooShort -> {
                                Text(
                                    text = "Имя должно быть длиннее 2 букв",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            isNameInvalidStart -> {
                                Text(
                                    text = "Имя должно начинаться с буквы (русской или английской)",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Разделитель
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(1.dp)
                            .background(Color(0xFFE0E0E0))
                    )

                    // Телефон
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Телефон",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                                BasicTextField(
                                    value = phone,
                                    onValueChange = { newValue ->
                                        // Фильтруем только цифры
                                        val digits = newValue.text.filter { it.isDigit() }
                                        // Форматируем текст
                                        val formatted = formatPhoneNumber(digits)
                                        // Вычисляем новую позицию курсора
                                        val digitsCount = digits.length
                                        val newSelection = calculateCursorPosition(formatted, digitsCount)
                                        // Обновляем значение с новой позицией курсора
                                        phone = TextFieldValue(
                                            text = formatted,
                                            selection = newSelection
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .padding(top = 4.dp),
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone
                                    )
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.right_arrow),
                                contentDescription = "Edit",
                                tint = Color(0xFFF58D4D),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (!isPhoneValid) {
                            Text(
                                text = "Телефон должен содержать минимум 11 цифр",
                                fontSize = 12.sp,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Разделитель
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(1.dp)
                            .background(Color(0xFFE0E0E0))
                    )

                    // Email
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Почта",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                                BasicTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .padding(top = 4.dp),
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    singleLine = true
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.right_arrow),
                                contentDescription = "Edit",
                                tint = Color(0xFFF58D4D),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (!isEmailValid && email.isNotEmpty()) {
                            Text(
                                text = "Некорректный формат email",
                                fontSize = 12.sp,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Кнопки внизу
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Кнопка "Сохранить"
                Button(
                    onClick = {
                        if (hasChanges) {
                            currentUser?.let { user ->
                                val updatedUser = user.copy(
                                    name = name,
                                    phone = viewModel.cleanPhoneNumber(phone.text),
                                    email = email
                                )
                                viewModel.updateUser(updatedUser)
                                // Показываем сообщение об успешном сохранении
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Данные успешно сохранены", duration = SnackbarDuration.Indefinite)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = hasChanges,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF58D4D),
                        disabledContainerColor = Color(0xFFB0B0B0)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Сохранить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasChanges) Color.White else Color(0xFF666666)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Кнопка "Выйти из аккаунта"
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "ВЫЙТИ ИЗ АККАУНТА",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Диалоговое окно подтверждения выхода
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = {
                        Text(
                            text = "Подтверждение выхода",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Вы уверены, что хотите выйти из аккаунта?",
                            fontSize = 16.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.logout()
                                showLogoutDialog = false
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000))
                        ) {
                            Text(
                                text = "Выйти",
                                color = Color.White
                            )
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showLogoutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666))
                        ) {
                            Text(
                                text = "Отмена",
                                color = Color.White
                            )
                        }
                    }
                )
            }
        }
    }
}