package com.example.hotel.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel.R
import com.example.hotel.data.Service
import com.example.hotel.data.viewmodel.HotelViewModel


data class Category(val imageRes: Int, val title: String)

@Composable
fun HomeScreen(viewModel: HotelViewModel, navController: NavHostController, modifier: Modifier = Modifier) {
    val currentUser by viewModel.currentUser.observeAsState()
    val userRole = currentUser?.role ?: "Guest"
    val services by viewModel.services.observeAsState(emptyList())

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(vertical = 5.dp)
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
                            .padding(start = 16.dp)
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
                .padding(paddingValues)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                // Проверка для гостя: если список акций пуст
                if (userRole == "Guest" && services.isEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(Modifier.height(30.dp))

                        Text(
                            text = "ПОКА НЕТ АКЦИЙ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(Modifier.height(100.dp))
                }
                // Проверка для менеджера: если список акций пуст
                else if (userRole == "Manager" && services.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Кнопка добавления акции
                        IconButton(
                            onClick = { navController.navigate("makerForService") },
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 30.dp,
                                        topEnd = 30.dp,
                                        bottomEnd = 5.dp,
                                        bottomStart = 5.dp
                                    )
                                )
                                .size(80.dp) // Квадратная кнопка

                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add_icon),
                                contentDescription = "Add",
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFFF58D4D)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        // Текст "ДОБАВЬТЕ ПЕРВУЮ АКЦИЮ"
                        Text(
                            text = "ДОБАВЬТЕ ПЕРВУЮ АКЦИЮ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(Modifier.height(30.dp))
                }
            }

            // Список акций с горизонтальной прокруткой
            if (userRole != "Guest" || services.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(services) { service ->
                            Box(
                                modifier = Modifier
                                    .width(310.dp)
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(
                                            topStart = 30.dp,
                                            topEnd = 30.dp,
                                            bottomEnd = 20.dp,
                                            bottomStart = 20.dp
                                        )
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 30.dp,
                                            topEnd = 30.dp,
                                            bottomEnd = 20.dp,
                                            bottomStart = 20.dp
                                        )
                                    )
                            ) {
                                ServiceItem(
                                    service = service,
                                    userRole = userRole,
                                    onEdit = { navController.navigate("makerForService/${service.serviceId}") },
                                    onDelete = { viewModel.deleteService(service.serviceId) },
                                    onClick = { navController.navigate("serviceDetail/${service.serviceId}") }
                                )
                            }
                        }

                        // Добавляем кнопку "+" в конец списка для Manager
                        if (userRole == "Manager" && services.isNotEmpty()) {
                            item {
                                IconButton(
                                    onClick = { navController.navigate("makerForService") },
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 30.dp,
                                                topEnd = 30.dp,
                                                bottomEnd = 5.dp,
                                                bottomStart = 5.dp
                                            )
                                        )
                                        .width(80.dp)
                                        .height(132.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.add_icon),
                                        contentDescription = "Add",
                                        modifier = Modifier.size(40.dp),
                                        tint = Color(0xFFF58D4D)
                                    )
                                }
                            }
                        }
                    }

                }
            }



            // Список категорий
            val categories = listOf(
                Category(R.drawable.restoran, "Рестораны"),
                Category(R.drawable.room, "Номера"),
                Category(R.drawable.hotel, "Отели"),
                Category(R.drawable.animations, "Анимации"),
                Category(R.drawable.taxi, "Трансфер"),
                Category(R.drawable.spa_image, "SPA")
            )

            items(categories.chunked(2)) { categoryPair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryPair.forEach { category ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomEnd = 5.dp, bottomStart = 5.dp))
                                .weight(1f)
                        ) {
                            CategoryItem(
                                imageRes = category.imageRes,
                                title = category.title,
                                onClick = {
                                    if (category.title == "Номера") {
                                        navController.navigate("rooms")
                                    } else if (category.title == "Трансфер") {
                                        navController.navigate("transfer")
                                    } else if (category.title == "Анимации") {
                                        navController.navigate("animation")
                                    } else if (category.title == "Рестораны") {
                                        navController.navigate("restaurant")
                                    } else if (category.title == "Отели") {
                                        navController.navigate("hotel")
                                    } else if (category.title == "SPA") {
                                        navController.navigate("spa")
                                    }
                                }
                            )
                        }
                    }

                    if (categoryPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun CategoryItem(imageRes: Int, title: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(5.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 5.dp, bottomEnd = 5.dp)),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Icon(
                painter = painterResource(id = R.drawable.bold_right_arrow),
                contentDescription = "Arrow",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFF58D4D)
            )
        }
    }
}

@Composable
fun ServiceItem(
    service: Service,
    userRole: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) } // Состояние для диалога подтверждения
    val hasSubtitle = service.subTitle.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = service.imageUrl,
                contentDescription = service.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 5.dp, bottomEnd = 5.dp)),
                contentScale = ContentScale.Crop,
                onError = { /* Обработка ошибки загрузки изображения */ }
            )

            if (userRole == "Manager") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            tint = Color.White,
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.3f),
                                    RoundedCornerShape(30.dp)
                                )
                                .size(25.dp),
                            contentDescription = "More"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Изменить", color = Color(0xFFF58D4D)) },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить", color = Color.Red) },
                            onClick = {
                                expanded = false
                                showDeleteDialog = true // Показываем диалог вместо немедленного удаления
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = service.name.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .height(20.dp)
                        .padding(start = 5.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = service.subTitle ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    modifier = Modifier
                        .height(30.dp)
                        .padding(start = 5.dp)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.bold_right_arrow),
                contentDescription = "Arrow",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFF58D4D)
            )
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Подтверждение удаления",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить акцию \"${service.name}\"?",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete() // Вызываем удаление после подтверждения
                    }
                ) {
                    Text(
                        text = "Удалить",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        text = "Отмена",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            },
            containerColor = Color.White
        )
    }
}