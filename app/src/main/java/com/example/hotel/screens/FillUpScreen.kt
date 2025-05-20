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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R
import java.util.Locale

@Composable
fun FillUpScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    var selectedBank by remember { mutableStateOf("T-Bank") } // По умолчанию выбран Т-Банк

    // Преобразуем сумму в число для валидации (удаляем пробелы)
    val cleanedText = textFieldValue.text.replace(" ", "")
    val amountValue = cleanedText.toIntOrNull() ?: 0
    // Кнопка активна, если сумма от 100 до 50 000
    val isAmountValid = amountValue in 100..50000

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Пополнение кошелька",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .padding(top = 38.dp)
            .background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Поле ввода суммы
                Text(
                    text = "Введите сумму",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "от 100₽ до 50 000₽",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        // Фильтруем только цифры
                        val digitsOnly = newValue.text.filter { it.isDigit() }
                        if (digitsOnly.isNotEmpty()) {
                            val number = digitsOnly.toLongOrNull() ?: 0
                            // Используем Locale.US для предсказуемого форматирования
                            val formatted = String.format(Locale.US, "%,d", number).replace(",", " ")
                            textFieldValue = textFieldValue.copy(
                                text = formatted,
                                selection = TextRange(formatted.length) // Курсор в конец
                            )
                        } else {
                            textFieldValue = textFieldValue.copy(
                                text = "",
                                selection = TextRange(0) // Курсор в начало при пустом поле
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Цифровая клавиатура
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "Сумма пополнения",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                )

                // Добавляем сообщение об ошибке, если сумма вне диапазона
                if (!isAmountValid && textFieldValue.text.isNotEmpty()) {
                    Text(
                        text = when {
                            amountValue < 100 -> "Минимальная сумма — 100 ₽"
                            amountValue > 50000 -> "Максимальная сумма — 50 000 ₽"
                            else -> "Введите корректное число"
                        },
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Кнопки для быстрого выбора суммы
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            val formatted = String.format(Locale.US, "%,d", 1000).replace(",", " ")
                            textFieldValue = textFieldValue.copy(
                                text = formatted,
                                selection = TextRange(formatted.length) // Курсор в конец
                            )
                        }, // Форматируем 1000
                        modifier = Modifier.weight(1f).padding(end = 5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "1 000 ₽",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            val formatted = String.format(Locale.US, "%,d", 2000).replace(",", " ")
                            textFieldValue = textFieldValue.copy(
                                text = formatted,
                                selection = TextRange(formatted.length) // Курсор в конец
                            )
                        }, // Форматируем 2000
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "2 000 ₽",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            val formatted = String.format(Locale.US, "%,d", 3000).replace(",", " ")
                            textFieldValue = textFieldValue.copy(
                                text = formatted,
                                selection = TextRange(formatted.length) // Курсор в конец
                            )
                        }, // Форматируем 3000
                        modifier = Modifier.weight(1f).padding(start = 5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "3 000 ₽",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                // Секция выбора банка
                Text(
                    text = "Выберите, откуда пополнить",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { selectedBank = "T-Bank" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.t_bank_image),
                        contentDescription = "T-Bank",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "Т-Банк",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    Checkbox(
                        checked = selectedBank == "T-Bank",
                        onCheckedChange = { if (it) selectedBank = "T-Bank" },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFF58D4D),
                            checkmarkColor = Color.White
                        )
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { selectedBank = "RNKB Bank" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rnkb_image),
                        contentDescription = "RNKB Bank",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "РНКБ Банк",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    Checkbox(
                        checked = selectedBank == "RNKB Bank",
                        onCheckedChange = { if (it) selectedBank = "RNKB Bank" },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFF58D4D),
                            checkmarkColor = Color.White,
                            uncheckedColor = Color(0xFFF58D4D)
                        )
                    )
                }
            }

            // Кнопка "Пополнить"
            Button(
                onClick = { /* Логика пополнения */ },
                enabled = isAmountValid, // Активна только при сумме от 100 до 50 000
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF58D4D),
                    disabledContainerColor = Color(0xFFCCCCCC) // Серый цвет для неактивной кнопки
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Пополнить",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}