package com.example.hotel.screens

import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    // Загружаем комнаты, бронирования и пользователей при входе пользователя
    LaunchedEffect(currentUser) {
        viewModel.loadRoomsAndBookings() // Оставляем только этот вызов
        viewModel.loadUsers()
        Log.d("RoomScreen", "After loading: bookings=$bookings, users=$users")
    }

    // Дополнительно обновляем UI, когда пользователи или бронирования изменяются
    LaunchedEffect(users, bookings) {
        Log.d("RoomScreen", "Users or bookings updated: bookings=$bookings, users=$users")
    }

    // Состояния для фильтров
    var showFilterDialog by remember { mutableStateOf(false) }
    var priceFrom by remember { mutableStateOf("") }
    var priceTo by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var capacity by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Состояние для диалога подтверждения удаления
    var showDeleteDialog by remember { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<Room?>(null) }

    // Состояние для диалога предупреждения
    var showWarningDialog by remember { mutableStateOf(false) }

    // Состояние для уведомления
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

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
    val filteredRooms by remember(searchQuery.value, visibleRooms, priceFrom, priceTo, selectedType, capacity, userRole, bookings) {
        derivedStateOf {
            visibleRooms.filter { room ->
                val isNotBooked = if (userRole == "Manager") true else {
                    val booking = bookings.find { it.roomId == room.roomId }
                    booking == null
                }

                val matchesSearch = searchQuery.value.isBlank() ||
                        room.name.lowercase().contains(searchQuery.value.lowercase()) ||
                        room.description.lowercase().contains(searchQuery.value.lowercase()) ||
                        room.type.lowercase().contains(searchQuery.value.lowercase())

                val priceFromValue = priceFrom.toFloatOrNull() ?: 0f
                val priceToValue = priceTo.toFloatOrNull() ?: 10000000f
                val matchesPrice = room.price in priceFromValue..priceToValue

                val matchesType = selectedType == null || room.type == selectedType

                val capacityValue = capacity.toIntOrNull() ?: 4
                val matchesCapacity = room.capacity <= capacityValue

                isNotBooked && matchesSearch && matchesPrice && matchesType && matchesCapacity
            }
        }
    }

    // Проверка активности кнопки "Применить"
    val isApplyEnabled by remember(priceFrom, priceTo, selectedType, capacity) {
        derivedStateOf {
            (priceFrom.isNotBlank() || priceTo.isNotBlank()) || selectedType != null || capacity.isNotBlank()
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog && roomToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить номер?", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Вы уверены, что хотите удалить номер \"${roomToDelete!!.name}\"?", fontSize = 16.sp, color = Color(0xFF666666)) },
            confirmButton = {
                TextButton(onClick = {
                    visibleRooms.remove(roomToDelete)
                    viewModel.deleteRoom(roomToDelete!!.roomId)
                    showDeleteDialog = false
                    roomToDelete = null
                }) {
                    Text("Удалить", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; roomToDelete = null }) {
                    Text("Отмена", color = Color(0xFFF58D4D), fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            title = { Text("Действие невозможно", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Номер забронирован. Вы не можете изменить или удалить его, пока бронирование активно.", fontSize = 16.sp, color = Color(0xFF666666)) },
            confirmButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text("ОК", color = Color(0xFFF58D4D), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp)
        )
    }

    // Диалог фильтров
    if (showFilterDialog) {
        Dialog(onDismissRequest = { showFilterDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Фильтр",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            priceFrom = ""
                            priceTo = ""
                            selectedType = null
                            capacity = ""
                            showFilterDialog = false
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = Color(0xFFF58D4D),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Фильтр по цене
                Text(
                    text = "Цена",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = priceFrom,
                        onValueChange = { priceFrom = it },
                        placeholder = { Text("От", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            disabledContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    TextField(
                        value = priceTo,
                        onValueChange = { priceTo = it },
                        placeholder = { Text("До", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            disabledContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Фильтр по типу
                Text(
                    text = "Тип",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RoomType.entries.filter { it != RoomType.LUXE }.forEach { type ->
                            Button(
                                onClick = { selectedType = if (selectedType == type.displayName) null else type.displayName },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedType == type.displayName) Color(0xFFF58D4D) else Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 10.sp,
                                    color = if (selectedType == type.displayName) Color.White else Color.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { selectedType = if (selectedType == RoomType.LUXE.displayName) null else RoomType.LUXE.displayName },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == RoomType.LUXE.displayName) Color(0xFFF58D4D) else Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = RoomType.LUXE.displayName,
                                fontSize = 14.sp,
                                color = if (selectedType == RoomType.LUXE.displayName) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Фильтр по вместимости
                Text(
                    text = "Вместимость",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = capacity,
                    onValueChange = { newValue ->
                        if (newValue.toIntOrNull() in 1..5 || newValue.isEmpty()) {
                            capacity = newValue
                        }
                    },
                    placeholder = { Text("От 1 до 5 человек", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE0E0E0),
                        unfocusedContainerColor = Color(0xFFE0E0E0),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопка "Применить"
                Button(
                    onClick = { showFilterDialog = false },
                    enabled = isApplyEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF58D4D),
                        disabledContainerColor = Color(0xFFCCCCCC)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
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
                        onClick = { showFilterDialog = true },
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

                                    val userMoney = money // Замените на реальную логику получения денег пользователя
                                    val roomPrice = room.price
                                    if (userMoney >= roomPrice) {
                                        viewModel.bookRoom(room, user.userId, checkInDate, checkOutDate, roomPrice)
                                        snackbarMessage = "Успешно забронирован номер! Перейдите в Профиль для подробной информации."
                                    } else {
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
    val booking = bookings.find { it.roomId == room.roomId }
    val bookedByUser = if (booking != null) {
        if (users.isEmpty()) {
            "Loading..." // Отображаем "Loading..." пока пользователи не загружены
        } else {
            users.find { it.userId == booking.userId }?.name ?: "Неизвестный пользователь"
        }
    } else {
        null
    }
    var showMenu by remember { mutableStateOf(false) }

    // Логирование для отладки
    Log.d("RoomItem", "Room ${room.roomId}: booking=$booking, bookedByUser=$bookedByUser, userRole=$userRole")

    val allPhotos = remember(room) { listOf(room.imageUrl) + room.additionalPhotos }
    var currentPhotoIndex by remember { mutableStateOf(0) }
    var offsetX by remember { mutableStateOf(0f) }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(Color.LightGray)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX < 0) {
                                    if (currentPhotoIndex < allPhotos.size - 1) {
                                        currentPhotoIndex++
                                    } else {
                                        currentPhotoIndex = 0
                                    }
                                } else if (offsetX > 0) {
                                    if (currentPhotoIndex > 0) {
                                        currentPhotoIndex--
                                    } else {
                                        currentPhotoIndex = allPhotos.size - 1
                                    }
                                }
                                offsetX = 0f
                            },
                            onDragCancel = { offsetX = 0f }
                        ) { _, dragAmount -> offsetX += dragAmount }
                    }
            ) {
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
                                text = { Text("Изменить", fontSize = 16.sp, color = if (booking == null) Color(0xFFF58D4D) else Color.Gray) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                enabled = booking == null
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить", fontSize = 16.sp, color = if (booking == null) Color.Red else Color.Gray) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                enabled = booking == null
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = highlightText(room.name.uppercase(), searchQuery),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

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

            if (userRole == "Manager") {
                if (booking != null || room.isBooked) { // Проверяем и booking, и isBooked
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
                    Button(
                        onClick = onSelect,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
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
            } else if (booking == null && !room.isBooked) {
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
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