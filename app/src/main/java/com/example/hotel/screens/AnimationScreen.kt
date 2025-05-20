package com.example.hotel.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R

// Модель данных для анимации
data class AnimationEvent(
    val time: String,
    val title: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun AnimationScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var selectedDate by remember { mutableStateOf(1) } // По умолчанию выбрана 1-я дата
    var expandedItem by remember { mutableStateOf<String?>(null) } // Состояние для раскрытия описания
    var isCalendarExpanded by remember { mutableStateOf(false) } // Состояние для скрытия/раскрытия календаря

    // Функция для обновления выбранной даты
    fun onDateSelected(day: Int) {
        if (day in 1..30) {
            selectedDate = day
        }
    }

    // Данные для анимаций с новыми описаниями
    val animations = mapOf(
        1 to listOf(
            AnimationEvent(
                time = "10:00-10:30",
                title = "Приветственный подъем с анимацией и знакомство (3+)",
                description = "Утро начинается с веселой анимации! Дети познакомятся с аниматорами и друг с другом через игры и танцы.",
                imageRes = R.drawable.good_morning
            ),
            AnimationEvent(
                time = "10:30-11:00",
                title = "Спортивная зарядка с тренером (3+)",
                description = "Энергичная зарядка на свежем воздухе под руководством тренера для всей семьи.",
                imageRes = R.drawable.animation_workout
            ),
            AnimationEvent(
                time = "11:00-12:00",
                title = "Party Time - конкурсы и танцы (3+)",
                description = "Яркая вечеринка с конкурсами, танцевальными баттлами и весельем под руководством аниматоров.",
                imageRes = R.drawable.animation_party
            )
        )
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box() {
                        Image(
                            painter = painterResource(id = R.drawable.logo_of_riviera),
                            contentDescription = "Логотип Riviera",
                            modifier = Modifier
                                .height(30.dp)
                                .align(Alignment.CenterStart)
                        )
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Анимации",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Start
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "<",
                        fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            if (selectedDate > 1) selectedDate -= 1
                        }
                    )
                    Text(
                        text = "$selectedDate июня",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ">",
                        fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            if (selectedDate < 31) selectedDate += 1
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCalendarExpanded = !isCalendarExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            repeat(5) { columnIndex ->
                                val day = selectedDate + columnIndex - (selectedDate - 1) % 5
                                val isSelected = day == selectedDate
                                Box(
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .clickable { onDateSelected(day) }
                                        .background(
                                            if (isSelected) Color(0xFFF58D4D) else Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day <= 30) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 14.sp,
                                            color = if (isSelected) Color.White else Color.Black,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (isCalendarExpanded) "▲" else "▼",
                            fontSize = 16.sp,
                            color = Color(0xFFF58D4D)
                        )
                    }
                    if (isCalendarExpanded) {
                        Calendar(selectedDate = selectedDate, onDateSelected = { day -> onDateSelected(day) })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (animations[selectedDate]?.isNotEmpty() == true) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Время",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF58D4D),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                Divider(
                                    color = Color(0xFFF58D4D),
                                    thickness = 1.dp,
                                    modifier = Modifier
                                        .width(2.dp)
                                        .fillMaxHeight()
                                )
                                Column(
                                    modifier = Modifier.weight(2f)
                                ) {
                                    Text(
                                        text = "Анимации",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF58D4D),
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                            Divider(
                                color = Color(0xFFF58D4D),
                                thickness = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            animations[selectedDate]?.forEach { event ->
                                val isExpanded = expandedItem == "$selectedDate-${event.time}"
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = event.time,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        Divider(
                                            color = Color(0xFFF58D4D),
                                            thickness = 1.dp,
                                            modifier = Modifier
                                                .width(2.dp)
                                                .fillMaxHeight()
                                        )
                                        Column(
                                            modifier = Modifier.weight(2f)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = event.title,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(end = 8.dp),
                                                    textAlign = TextAlign.Start
                                                )
                                                Text(
                                                    text = if (isExpanded) "▲" else "▼",
                                                    fontSize = 16.sp,
                                                    color = Color(0xFFF58D4D),
                                                    modifier = Modifier.clickable { expandedItem = if (isExpanded) null else "$selectedDate-${event.time}" }
                                                )
                                            }
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = event.imageRes),
                                                contentDescription = event.title,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = event.description,
                                                fontSize = 14.sp,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp)
                                            )
                                        }
                                    }
                                    Divider(
                                        color = Color(0xFFF58D4D),
                                        thickness = 1.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "На $selectedDate июня анимаций не запланировано, к сожалению",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun Calendar(selectedDate: Int, onDateSelected: (Int) -> Unit) {
    val daysInMonth = 30 // Устанавливаем 30 дней
    val daysPerRow = 5 // 5 чисел в строке
    val rows = 6 // 6 строк

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(rows - 1) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(daysPerRow) { columnIndex ->
                        val day = rowIndex * daysPerRow + columnIndex + 1
                        if (day <= daysInMonth) {
                            val isSelected = day == selectedDate
                            Box(
                                modifier = Modifier
                                    .weight(0.7f)
                                    .clickable { onDateSelected(day) }
                                    .background(
                                        if (isSelected) Color(0xFFF58D4D) else Color.White,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color.White else Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(0.7f)
                                    .background(Color(0xFF808080))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}