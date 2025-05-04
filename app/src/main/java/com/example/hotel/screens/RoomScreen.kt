package com.example.hotel.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel.R
import com.example.hotel.data.Booking
import com.example.hotel.data.Room
import com.example.hotel.data.User
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
    val bookings by viewModel.bookings.observeAsState(emptyList())
    val users by viewModel.users.observeAsState(emptyList())

    // Локальное состояние для анимации удаления
    val visibleRooms = remember { mutableStateListOf<Room>() }
    LaunchedEffect(rooms) {
        visibleRooms.clear()
        visibleRooms.addAll(rooms)
    }


    LaunchedEffect(bookings) {
        viewModel.loadRooms()
    }

    // Загружаем данные при открытии экрана
    LaunchedEffect(currentUser) {
        viewModel.loadRooms()
        viewModel.loadUsers()
        if (userRole == "Manager") {
            viewModel.loadAllBookings() // Для менеджера загружаем все бронирования
        } else {
            currentUser?.userId?.let { userId ->
                viewModel.loadBookings(userId) // Для обычного пользователя загружаем только его бронирования
            }
        }
    }

    // Состояния для фильтров
    var showFilterSheet by remember { mutableStateOf(false) }
    var priceRange by remember { mutableFloatStateOf(10000000f) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var capacity by remember { mutableIntStateOf(4) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Состояние для диалога подтверждения удаления
    var showDeleteDialog by remember { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<Room?>(null) }

    // Состояние для диалога предупреждения
    var showWarningDialog by remember { mutableStateOf(false) }

    // Состояние для уведомления
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    // Загружаем данные при открытии экрана
    LaunchedEffect(currentUser) {
        viewModel.loadRooms()
        viewModel.loadUsers()
        if (userRole == "Manager") {
            viewModel.loadAllBookings() // Для менеджера загружаем все бронирования
        } else {
            currentUser?.userId?.let { userId ->
                viewModel.loadBookings(userId) // Для обычного пользователя загружаем только его бронирования
            }
        }
    }

    // Обработка ошибок
    val errorMessage by viewModel.errorMessage.observeAsState()
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            viewModel.clearError()
        }
    }

    // Показ уведомления
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(snackbarMessage)
                snackbarMessage = ""
            }
        }
    }

    // Фильтрация номеров
    val filteredRooms by remember(searchQuery.value, visibleRooms, priceRange, selectedType, capacity, userRole, bookings) {
        derivedStateOf {
            visibleRooms.filter { room ->
                // Для менеджера показываем все номера, для остальных — только незанятые
                val isNotBooked = if (userRole == "Manager") true else {
                    val booking = bookings.find { it.roomId == room.roomId }
                    booking == null // Гость видит только свободные номера
                }

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

    // Диалог предупреждения для менеджера
    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = {
                Text(
                    text = "Действие невозможно",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Номер забронирован. Вы не можете изменить или удалить его, пока бронирование активно.",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showWarningDialog = false }
                ) {
                    Text(
                        text = "ОК",
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

    // Модальное окно фильтров
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Фильтры",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Фильтр по цене
                Text(
                    text = "Цена до: ${priceRange.toInt()} ₽",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = 100f..10000000f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFF58D4D),
                        activeTrackColor = Color(0xFFF58D4D),
                        inactiveTrackColor = Color(0xFFE0E0E0)
                    )
                )

                // Фильтр по типу комнаты
                Text(
                    text = "Тип комнаты",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RoomType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type.displayName,
                            onClick = {
                                selectedType = if (selectedType == type.displayName) null else type.displayName
                            },
                            label = {
                                Text(
                                    text = type.displayName,
                                    fontSize = 14.sp,
                                    color = if (selectedType == type.displayName) Color.White else Color.Black
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF58D4D),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                // Фильтр по вместимости
                Text(
                    text = "Вместимость до: $capacity чел",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Slider(
                    value = capacity.toFloat(),
                    onValueChange = { capacity = it.toInt() },
                    valueRange = 1f..4f,
                    steps = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFF58D4D),
                        activeTrackColor = Color(0xFFF58D4D),
                        inactiveTrackColor = Color(0xFFE0E0E0)
                    )
                )

                // Кнопки для сброса и применения фильтров
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            priceRange = 10000000f
                            selectedType = null
                            capacity = 4
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Сбросить",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }
                            showFilterSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Применить",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) {
                Snackbar(
                    modifier = Modifier.background(Color(0xFFF58D4D)),
                    containerColor = Color(0xFFF58D4D),
                    contentColor = Color.White
                ) {
                    Text(
                        text = it.visuals.message,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        },
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
                            bookings = bookings,
                            users = users,
                            onEdit = {
                                if (bookings.any { it.roomId == room.roomId }) {
                                    showWarningDialog = true
                                } else {
                                    navController.navigate("makerForRooms/${room.roomId}")
                                }
                            },
                            onDelete = {
                                if (bookings.any { it.roomId == room.roomId }) {
                                    showWarningDialog = true
                                } else {
                                    roomToDelete = room
                                    showDeleteDialog = true
                                }
                            },
                            onSelect = {
                                currentUser?.let { user ->
                                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                    val checkInDate = LocalDate.now().format(formatter)
                                    val checkOutDate = LocalDate.now().plusDays(1).format(formatter)

                                    // Проверяем, достаточно ли средств
                                    val userMoney = money
                                    val roomPrice = room.price
                                    if (userMoney >= roomPrice) {
                                        // Достаточно средств — бронируем
                                        viewModel.bookRoom(room, user.userId, checkInDate, checkOutDate, roomPrice)
                                        // Обновляем список номеров после бронирования
                                        viewModel.loadRooms()
                                        snackbarMessage = "Успешно забронирован номер! Перейдите в Профиль для подробной информации."
                                    } else {
                                        // Недостаточно средств — показываем уведомление
                                        val shortfall = roomPrice - userMoney
                                        snackbarMessage = "Не удалось забронировать номер. Недостаточно средств. Не хватает: ${shortfall.toInt()} ₽."
                                    }
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
}

@Composable
fun RoomItem(
    room: Room,
    userRole: String,
    users: List<User>,
    searchQuery: String,
    bookings: List<Booking>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Находим бронирование для текущей комнаты
    val booking = bookings.find { it.roomId == room.roomId }
    val bookedByUser = if (booking != null) { users.find { it.userId == booking.userId }?.name ?: "Неизвестный пользователь" } else { null }
    var showMenu by remember { mutableStateOf(false) }

    // Список всех фотографий (основная + дополнительные)
    val allPhotos = remember(room) {
        listOf(room.imageUrl) + room.additionalPhotos
    }

// Состояние для текущего индекса фотографии
    var currentPhotoIndex by remember { mutableStateOf(0) }

// Состояние для смещения при свайпе
    var offsetX by remember { mutableStateOf(0f) }

// Анимированное значение смещения
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 600),
        label = "offsetXAnimation"
    )

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
            // Карусель фотографий номера
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(Color.LightGray)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                // Определяем направление свайпа по конечному смещению
                                if (offsetX < 0) { // Свайп влево
                                    if (currentPhotoIndex < allPhotos.size - 1) {
                                        currentPhotoIndex++
                                    } else {
                                        currentPhotoIndex = 0
                                    }
                                } else if (offsetX > 0) { // Свайп вправо
                                    if (currentPhotoIndex > 0) {
                                        currentPhotoIndex--
                                    } else {
                                        currentPhotoIndex = allPhotos.size - 1
                                    }
                                }
                                // Сбрасываем смещение после завершения свайпа
                                offsetX = 0f
                            },
                            onDragCancel = {
                                // Сбрасываем смещение, если свайп отменён
                                offsetX = 0f
                            }
                        ) { _, dragAmount ->
                            // Обновляем смещение во время свайпа
                            offsetX += dragAmount
                        }
                    }
            ) {
                // Используем Crossfade для плавного перехода между изображениями
                Crossfade(
                    targetState = allPhotos[currentPhotoIndex],
                    animationSpec = tween(durationMillis = 600),
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(animatedOffsetX.toInt(), 0) }
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                ) { currentImageUrl ->
                    AsyncImage(
                        model = currentImageUrl,
                        contentDescription = "Room Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Индикаторы (кружочки)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allPhotos.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPhotoIndex) Color(0xFFF58D4D)
                                    else Color.White
                                )
                                .padding(horizontal = 2.dp)
                        )
                    }
                }

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
                                        color = if (bookedByUser == null) Color(0xFFF58D4D) else Color.Gray
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                enabled = bookedByUser == null
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Удалить",
                                        fontSize = 16.sp,
                                        color = if (bookedByUser == null) Color.Red else Color.Gray
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                enabled = bookedByUser == null
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
                    painter = painterResource(id = R.drawable.ic_star),
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
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Capacity",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
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

            // Кнопка или текст "Забронировано"
            if (userRole == "Manager") {
                if (bookedByUser != null) {
                    // Если номер забронирован, показываем текст
                    Text(
                        text = "Забронировано: $bookedByUser",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF58D4D),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                } else {
                    // Если номер свободен, показываем кнопку для менеджера
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
            } else if (bookedByUser == null) {
                // Кнопка "Выбрать номер" для гостя, если номер свободен
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

}

@Composable
fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text)
    }

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var index = lowerText.indexOf(lowerQuery)
    val indices = mutableListOf<Int>()

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