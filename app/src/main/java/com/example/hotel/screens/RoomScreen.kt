package com.example.hotel.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel.R
import com.example.hotel.data.Room
import com.example.hotel.data.entity.RoomType
import com.example.hotel.data.viewmodel.HotelViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    viewModel: HotelViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.observeAsState()
    val userRole = currentUser?.role ?: "Guest"
    val searchQuery = remember { mutableStateOf("") }
    val rooms by viewModel.rooms.observeAsState(emptyList())

    // Локальное состояние для анимации удаления
    val visibleRooms = remember { mutableStateListOf<Room>() }
    LaunchedEffect(rooms) {
        visibleRooms.clear()
        visibleRooms.addAll(rooms)
    }

    // Состояния для фильтров
    var showFilterSheet by remember { mutableStateOf(false) }
    var priceRange by remember { mutableFloatStateOf(10000000f) } // Максимальная цена для фильтра
    var selectedType by remember { mutableStateOf<String?>(null) } // Выбранный тип номера
    var capacity by remember { mutableIntStateOf(4) } // Максимальная вместимость
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Состояние для диалога подтверждения удаления
    var showDeleteDialog by remember { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<Room?>(null) }

    // Загружаем номера при открытии экрана
    LaunchedEffect(Unit) {
        viewModel.loadRooms()
    }

    // Обработка ошибок
    val errorMessage by viewModel.errorMessage.observeAsState()
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            viewModel.clearError()
        }
    }

    // Фильтрация номеров
    val filteredRooms by remember(searchQuery.value, visibleRooms, priceRange, selectedType, capacity) {
        derivedStateOf {
            visibleRooms.filter { room ->
                // Условие для показа только незанятых номеров
                val isNotBooked = !room.isBooked

                val matchesSearch = searchQuery.value.isBlank() ||
                        room.name.lowercase().contains(searchQuery.value.lowercase()) ||
                        room.description.lowercase().contains(searchQuery.value.lowercase()) ||
                        room.type.lowercase().contains(searchQuery.value.lowercase())

                val matchesPrice = room.price <= priceRange
                val matchesType = selectedType == null || room.type == selectedType
                val matchesCapacity = room.capacity <= capacity

                isNotBooked && matchesSearch && matchesPrice && matchesType && matchesCapacity
            }
        }
    }

    // Функция для подсветки совпадений в тексте
    fun highlightText(text: String, query: String): AnnotatedString {
        if (query.isBlank()) {
            return AnnotatedString(text)
        }

        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        val indices = mutableListOf<Int>()
        var index = lowerText.indexOf(lowerQuery)

        while (index >= 0) {
            indices.add(index)
            index = lowerText.indexOf(lowerQuery, index + 1)
        }

        if (indices.isEmpty()) {
            return AnnotatedString(text)
        }

        return buildAnnotatedString {
            var lastIndex = 0
            for (i in indices) {
                if (lastIndex < i) {
                    append(text.substring(lastIndex, i))
                }
                val start = i
                val end = i + query.length
                pushStyle(SpanStyle(background = Color.Yellow))
                append(text.substring(start, end))
                pop()
                lastIndex = end
            }
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog && roomToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Удалить номер?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить номер \"${roomToDelete!!.name}\"?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        visibleRooms.remove(roomToDelete)
                        viewModel.deleteRoom(roomToDelete!!.roomId)
                        showDeleteDialog = false
                        roomToDelete = null
                    }
                ) {
                    Text(
                        text = "Удалить",
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        roomToDelete = null
                    }
                ) {
                    Text(
                        text = "Отмена",
                        color = Color(0xFFF58D4D),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp)
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF58D4D))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        TextField(
                            value = searchQuery.value,
                            onValueChange = { searchQuery.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            placeholder = {
                                Text(
                                    text = "Поиск номеров...",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Gray
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                    }
                    // Кнопка фильтров
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filtr_icon),
                            contentDescription = "Filter Rooms",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Заголовок "Номера" и кнопка добавления
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Номера",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )

                    if (userRole != "Guest") {
                        IconButton(
                            onClick = {
                                navController.navigate("makerForRooms")
                            },
                            modifier = Modifier
                                .size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add_icon),
                                contentDescription = "Add Room",
                                tint = Color(0xFFF58D4D),
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
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
            if (filteredRooms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (searchQuery.value.isBlank()) {
                                    if (userRole == "Manager") "Добавьте первый номер!" else "Номеров пока нет"
                                } else {
                                    "Номеров не найдено"
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            } else {
                itemsIndexed(filteredRooms, key = { _, room -> room.roomId }) { _, room ->
                    AnimatedVisibility(
                        visible = room in visibleRooms,
                        exit = slideOutVertically(
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 300)
                        )
                    ) {
                        RoomItem(
                            room = room,
                            userRole = userRole,
                            searchQuery = searchQuery.value,
                            onEdit = {
                                navController.navigate("makerForRooms/${room.roomId}")
                            },
                            onDelete = {
                                roomToDelete = room
                                showDeleteDialog = true
                            },
                            onSelect = {
                                currentUser?.let { user ->
                                    // Получаем текущую дату и следующий день
                                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                    val checkInDate = LocalDate.now().format(formatter)
                                    val checkOutDate = LocalDate.now().plusDays(1).format(formatter)
                                    viewModel.bookRoom(room, user.userId, checkInDate, checkOutDate, money)
                                } ?: run {
                                    viewModel.setErrorMessage("Пожалуйста, войдите в систему для бронирования")
                                }
                            }
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Модальное окно для фильтров
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Фильтры",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(16.dp))

                // Фильтр по цене
                Text(
                    text = "Максимальная цена: ${priceRange.toInt()} руб.",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Slider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = 100f..10000000f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Фильтр по типу номера
                Text(
                    text = "Тип номера",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoomType.entries.forEach { roomType ->
                        val isSelected = selectedType == roomType.displayName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color(0xFFF58D4D) else Color(0xFFE0E0E0),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedType = if (isSelected) null else roomType.displayName
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = roomType.displayName,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Фильтр по вместимости
                Text(
                    text = "Максимальная вместимость: $capacity чел.",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Slider(
                    value = capacity.toFloat(),
                    onValueChange = { capacity = it.toInt() },
                    valueRange = 1f..4f,
                    steps = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Кнопка применения фильтров
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showFilterSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF58D4D)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Применить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RoomItem(
    room: Room,
    userRole: String,
    searchQuery: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Изображение номера
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            ) {
                AsyncImage(
                    model = room.imageUrl,
                    contentDescription = "Room Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Троеточие (меню) для Manager
                if (userRole == "Manager") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .background(
                                    Color.Gray.copy(alpha = 0.3f),
                                    RoundedCornerShape(30.dp)
                                )
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Изменить",
                                        fontSize = 16.sp,
                                        color = Color(0xFFF58D4D)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Удалить",
                                        fontSize = 16.sp,
                                        color = Color.Red
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Название номера с подсветкой
            Text(
                text = highlightText(room.name.uppercase(), searchQuery),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Тип номера с иконкой звезды
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star), // Предполагается иконка звезды
                    contentDescription = "Room Type",
                    tint = Color(0xFFF58D4D),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = highlightText(room.type, searchQuery),
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Вместимость и цена
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person), // Предполагается иконка человека
                    contentDescription = "Capacity",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "до ${room.capacity} чел",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "от ${room.price.toInt()} ₽",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF58D4D)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Кнопка "Выбрать номер"
            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF58D4D)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Выбрать номер",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// Функция для подсветки совпадений в тексте
@Composable
fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text)
    }

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    val indices = mutableListOf<Int>()
    var index = lowerText.indexOf(lowerQuery)

    while (index >= 0) {
        indices.add(index)
        index = lowerText.indexOf(lowerQuery, index + 1)
    }

    if (indices.isEmpty()) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        var lastIndex = 0
        for (i in indices) {
            if (lastIndex < i) {
                append(text.substring(lastIndex, i))
            }
            val start = i
            val end = i + query.length
            pushStyle(SpanStyle(background = Color.Yellow))
            append(text.substring(start, end))
            pop()
            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
