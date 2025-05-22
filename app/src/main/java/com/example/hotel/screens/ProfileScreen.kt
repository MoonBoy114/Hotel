package com.example.hotel.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel.R
import com.example.hotel.data.viewmodel.HotelViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


var money: Double = 100000.0

@Composable
fun ProfileScreen(viewModel: HotelViewModel, navController: NavHostController, modifier: Modifier = Modifier) {
    val currentUser by viewModel.currentUser.observeAsState()
    val bookings by viewModel.bookings.observeAsState(emptyList())
    val rooms by viewModel.rooms.observeAsState(emptyList())

    val currentName = currentUser?.name

    // Загружаем данные и проверяем даты
    LaunchedEffect(Unit) {
        viewModel.loadRooms()
        currentUser?.let { viewModel.loadBookings(it.userId) }
    }

// Фильтруем бронирования: показываем только те, у которых checkOutDate ещё не наступил (строго больше текущей даты)
    val activeBookings = bookings.filter { booking ->
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val currentDate = LocalDate.now().format(formatter)
        currentDate > booking.checkOutDate
    }



    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(vertical = 5.dp) // Отступы для логотипа и иконки
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_of_riviera),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(30.dp)
                            .padding(start = 16.dp) // Отступ слева для логотипа
                    )

                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues) // Учитываем отступы от topBar
        ) {
            item {
                Spacer(Modifier.height(16.dp)) // Отступ сверху после topBar
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
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
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (currentName != null) {
                        Text(
                            text = currentName,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .clickable{ navController.navigate("aboutMe") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.small_profile),
                        contentDescription = "small profile",
                        modifier = Modifier.padding(8.dp).size(30.dp)
                    )

                    Text(
                        text = "Мои данные",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {},
                        modifier = Modifier.padding(start = 140.dp).size(25.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.right_arrow),
                            contentDescription = "right arrow",
                            tint = Color(0xFFF58D4D)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(10.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RS Кошелёк",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF666666)
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.right_arrow),
                                contentDescription = "right arrow",
                                tint = Color(0xFFF58D4D),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(16.dp)
                            )
                        }

                        Text(
                            text = "${money.toInt()} ₽",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = { navController.navigate("fillUp") },
                        modifier = Modifier
                            .height(36.dp)
                            .padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Пополнить",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Ваши бронирования",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (activeBookings.isEmpty() || currentUser?.role == "Manager") {
                        Text(
                            text = "Тут отобразятся бронирования пользователя",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Отображение активных бронирований
            items(activeBookings) { booking ->
                val room = rooms.find { it.roomId == booking.roomId }
                if (room != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Изображение комнаты (уменьшенное)
                        AsyncImage(
                            model = room.imageUrl,
                            contentDescription = "Room Image",
                            modifier = Modifier
                                .size(100.dp) // Уменьшаем размер изображения
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Текстовая информация
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f)
                        ) {
                            // Тип номера (оранжевый)
                            Text(
                                text = room.type,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF58D4D) // Оранжевый цвет для типа номера
                            )

                            // Название номера (на следующей строке)
                            Text(
                                text = room.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            // Количество человек и цена на одной строке с промежутком
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_person),
                                        contentDescription = "Capacity",
                                        tint = Color(0xFF666666),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${room.capacity} чел",
                                        fontSize = 14.sp,
                                        color = Color(0xFF666666),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                                Spacer(Modifier.weight(1f)) // Пространство между количеством людей и ценой
                                Text(
                                    text = "${booking.totalPrice.toInt()} ₽",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF58D4D)
                                )
                            }

                            // Даты бронирования
                            Text(
                                text = "Забронировано до ${booking.checkOutDate}",
                                fontSize = 13.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {navController.navigate("services") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_help),
                            contentDescription = "settings icon",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(30.dp),
                            tint = Color(0xFFF58D4D)

                        )

                        Text(
                            text = "Сервисы",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.right_arrow),
                            contentDescription = "right arrow",
                            tint = Color(0xFFF58D4D),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(horizontal = 16.dp)
                            .background(Color(0xFFE0E0E0))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("devices") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_devices),
                            contentDescription = "support icon",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(30.dp),
                            tint = Color(0xFFF58D4D)
                        )

                        Text(
                            text = "Устройства",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.right_arrow),
                            contentDescription = "right arrow",
                            tint = Color(0xFFF58D4D),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}