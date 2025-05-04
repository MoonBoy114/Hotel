package com.example.hotel.makers

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel.HotelApp
import com.example.hotel.R
import com.example.hotel.data.Room
import com.example.hotel.data.entity.RoomType
import com.example.hotel.data.viewmodel.HotelViewModel
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MakerForRooms(
    viewModel: HotelViewModel,
    navController: NavHostController,
    roomId: String? = null,
    modifier: Modifier = Modifier
) {
    // Текущие значения полей
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(RoomType.STANDARD.displayName) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    val additionalPhotos = remember { mutableStateListOf<String>() }

    // Исходные значения полей (для сравнения изменений)
    var originalName by remember { mutableStateOf("") }
    var originalType by remember { mutableStateOf("") }
    var originalDescription by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var originalCapacity by remember { mutableStateOf("") }
    var originalImageUrl by remember { mutableStateOf("") }
    val originalAdditionalPhotos = remember { mutableStateListOf<String>() }

    var isMainImageLoading by remember { mutableStateOf(false) }
    val isAdditionalImageLoading =
        remember { mutableStateListOf<Boolean>().apply { repeat(9) { add(false) } } }
    // Триггер для пересчёта изменений в additionalPhotos
    var photoListTrigger by remember { mutableStateOf(0) }

    // Состояния для сообщений об ошибках
    var nameError by remember { mutableStateOf<String?>(null) }
    var typeError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var capacityError by remember { mutableStateOf<String?>(null) }
    var imageUrlError by remember { mutableStateOf<String?>(null) }
    var additionalPhotosError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val app = context.applicationContext as HotelApp
    val storage = HotelApp.storage
    val coroutineScope = rememberCoroutineScope()

    // Загружаем данные номера, если передан roomId
    LaunchedEffect(roomId) {
        if (roomId != null) {
            viewModel.rooms.value?.find { it.roomId == roomId }?.let { room ->
                name = room.name
                type = room.type
                description = room.description
                price = room.price.toString()
                capacity = room.capacity.toString()
                imageUrl = room.imageUrl
                additionalPhotos.clear()
                additionalPhotos.addAll(room.additionalPhotos)

                // Сохраняем исходные значения
                originalName = room.name
                originalType = room.type
                originalDescription = room.description
                originalPrice = room.price.toString()
                originalCapacity = room.capacity.toString()
                originalImageUrl = room.imageUrl
                originalAdditionalPhotos.clear()
                originalAdditionalPhotos.addAll(room.additionalPhotos)

                // Обновляем триггер, чтобы учесть начальные данные
                photoListTrigger++
            }
        }
    }

    // Лаунчер для выбора основной фотографии
    val pickMainImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            imageUrlError = "Изображение не выбрано"
            return@rememberLauncherForActivityResult
        }
        isMainImageLoading = true

        coroutineScope.launch {
            try {
                val fileId = UUID.randomUUID().toString()
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when (mimeType) {
                    "image/jpeg", "image/jpg" -> "jpg"
                    "image/png" -> "png"
                    "image/gif" -> "gif"
                    else -> "jpg"
                }

                var fileName = uri.lastPathSegment ?: "image_$fileId"
                if (!fileName.contains(".")) {
                    fileName = "$fileName.$extension"
                } else {
                    val currentExtension = fileName.substringAfterLast(".").lowercase()
                    if (currentExtension != extension) {
                        fileName = fileName.substringBeforeLast(".") + ".$extension"
                    }
                }

                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                val file = bytes?.let {
                    io.appwrite.models.InputFile.fromBytes(
                        bytes = it,
                        filename = fileName,
                        mimeType = mimeType,
                    )
                } ?: throw IllegalStateException("Не удалось прочитать файл")

                val result = storage.createFile(
                    bucketId = "images",
                    fileId = fileId,
                    file = file
                )

                val fileUrl = "${HotelApp.client.endpoint}/storage/buckets/images/files/$fileId/view?project=${HotelApp.projectId}"
                imageUrl = fileUrl
                isMainImageLoading = false
            } catch (e: AppwriteException) {
                viewModel.setErrorMessage("Ошибка загрузки основной фотографии: ${e.message}")
                imageUrlError = "Ошибка загрузки: ${e.message}"
                isMainImageLoading = false
            } catch (e: Exception) {
                viewModel.setErrorMessage("Неожиданная ошибка при загрузке: ${e.message}")
                imageUrlError = "Неожиданная ошибка: ${e.message}"
                isMainImageLoading = false
            }
        }
    }

    // Лаунчер для выбора дополнительных фотографий
    val pickAdditionalImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) {
            additionalPhotosError = "Не выбрано ни одного изображения"
            return@rememberLauncherForActivityResult
        }
        uris.forEach { uri ->
            if (additionalPhotos.size < 9) {
                isAdditionalImageLoading.add(true)
                val adjustedIndex = isAdditionalImageLoading.size - 1

                coroutineScope.launch {
                    try {
                        val fileId = UUID.randomUUID().toString()
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        val extension = when (mimeType) {
                            "image/jpeg", "image/jpg" -> "jpg"
                            "image/png" -> "png"
                            "image/gif" -> "gif"
                            else -> "jpg"
                        }

                        var fileName = uri.lastPathSegment ?: "additional_image_$fileId"
                        if (!fileName.contains(".")) {
                            fileName = "$fileName.$extension"
                        } else {
                            val currentExtension = fileName.substringAfterLast(".").lowercase()
                            if (currentExtension != extension) {
                                fileName = fileName.substringBeforeLast(".") + ".$extension"
                            }
                        }

                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()

                        val file = bytes?.let {
                            io.appwrite.models.InputFile.fromBytes(
                                bytes = it,
                                filename = fileName,
                                mimeType = mimeType,
                            )
                        } ?: throw IllegalStateException("Не удалось прочитать файл")

                        val result = storage.createFile(
                            bucketId = "images",
                            fileId = fileId,
                            file = file
                        )

                        val fileUrl = "${HotelApp.client.endpoint}/storage/buckets/images/files/$fileId/view?project=${HotelApp.projectId}"
                        additionalPhotos.add(fileUrl)
                        photoListTrigger++
                        isAdditionalImageLoading[adjustedIndex] = false
                    } catch (e: AppwriteException) {
                        viewModel.setErrorMessage("Ошибка загрузки дополнительной фотографии: ${e.message}")
                        additionalPhotosError = "Ошибка загрузки: ${e.message}"
                        isAdditionalImageLoading[adjustedIndex] = false
                    } catch (e: Exception) {
                        viewModel.setErrorMessage("Неожиданная ошибка при загрузке: ${e.message}")
                        additionalPhotosError = "Неожиданная ошибка: ${e.message}"
                        isAdditionalImageLoading[adjustedIndex] = false
                    }
                }
            } else {
                additionalPhotosError = "Нельзя добавить больше 9 фотографий"
            }
        }
    }

    // Валидация формы
    val isFormValid by remember(name, type, description, price, capacity, imageUrl, additionalPhotos) {
        nameError = null
        typeError = null
        descriptionError = null
        priceError = null
        capacityError = null
        imageUrlError = null
        additionalPhotosError = null

        val nameValid = name.isNotBlank() && name.length <= 20
        if (!name.isNotBlank()) {
            nameError = "Название обязательно"
        } else if (name.length > 20) {
            nameError = "Название не должно превышать 20 символов"
        }

        val typeValid = RoomType.fromDisplayName(type) != null
        if (!typeValid) {
            typeError = "Выберите допустимый тип номера"
        }

        val descriptionValid = description.isNotBlank() && description.length <= 100
        if (!description.isNotBlank()) {
            descriptionError = "Описание обязательно"
        } else if (description.length > 100) {
            descriptionError = "Описание не должно превышать 100 символов"
        }

        val priceValid = price.toFloatOrNull() != null && price.toFloat() in 100f..10000000f
        if (price.isBlank()) {
            priceError = "Цена обязательна"
        } else if (price.toFloatOrNull() == null) {
            priceError = "Цена должна быть числом"
        } else if (price.toFloat() !in 100f..10000000f) {
            priceError = "Цена должна быть от 100 до 10,000,000 рублей"
        }

        val capacityValid = capacity.toIntOrNull() != null && capacity.toInt() in 1..4
        if (capacity.isBlank()) {
            capacityError = "Вместимость обязательна"
        } else if (capacity.toIntOrNull() == null) {
            capacityError = "Вместимость должна быть числом"
        } else if (capacity.toInt() !in 1..4) {
            capacityError = "Вместимость должна быть от 1 до 4 человек"
        }

        val imageUrlValid = imageUrl.isNotBlank()
        if (!imageUrlValid && !isMainImageLoading) {
            imageUrlError = "Основная фотография обязательна"
        }

        val additionalPhotosValid = additionalPhotos.size <= 9
        if (!additionalPhotosValid) {
            additionalPhotosError = "Не более 9 дополнительных фотографий"
        }

        mutableStateOf(
            nameValid && typeValid && descriptionValid && priceValid && capacityValid && imageUrlValid && additionalPhotosValid
        )
    }


    val hasChanges by remember(
        name, type, description, price, capacity, imageUrl, photoListTrigger
    ) {
        mutableStateOf(
            name != originalName ||
                    type != originalType ||
                    description != originalDescription ||
                    price != originalPrice ||
                    capacity != originalCapacity ||
                    imageUrl != originalImageUrl ||
                    additionalPhotos.size != originalAdditionalPhotos.size ||
                    additionalPhotos.toList() != originalAdditionalPhotos.toList()
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 35.dp)
            .navigationBarsPadding(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(vertical = 10.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { navController.popBackStack() }
                    )

                    Text(
                        text = "Конструктор для номеров",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
            ) {
                Button(
                    onClick = {
                        val room = Room(
                            roomId = roomId ?: "",
                            name = name,
                            type = type,
                            description = description,
                            price = price.toFloatOrNull() ?: 0f,
                            capacity = capacity.toIntOrNull() ?: 1,
                            imageUrl = imageUrl,
                            additionalPhotos = additionalPhotos
                        )
                        if (roomId == null) {
                            viewModel.insertRoom(room)
                        } else {
                            viewModel.updateRoom(room)
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF58D4D),
                        disabledContainerColor = Color(0xFFB0B0B0)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = if (roomId == null) isFormValid else (isFormValid && hasChanges)
                ) {
                    Text(
                        text = if (roomId == null) "Добавить" else "Изменить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (roomId == null) {
                            if (isFormValid) Color.White else Color(0xFF666666)
                        } else {
                            if (isFormValid && hasChanges) Color.White else Color(0xFF666666)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Основная фотография
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Основная фотография",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.White, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isMainImageLoading -> {
                                CircularProgressIndicator(
                                    color = Color(0xFFF58D4D),
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            imageUrl.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { pickMainImageLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.add_icon),
                                        contentDescription = "Add Photo",
                                        tint = Color(0xFFF58D4D),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Main Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop,
                                        onError = { error ->
                                            imageUrlError = "Ошибка загрузки изображения: ${error.result.throwable.message}"
                                        }
                                    )
                                    IconButton(
                                        onClick = { imageUrl = "" },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.delete_icon),
                                            contentDescription = "Delete Image",
                                            tint = Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    imageUrlError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Поле для названия
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)

                ) {
                    Text(
                        text = "Название",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { if (it.length <= 20) name = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (name.isEmpty()) {
                                    Text(
                                        text = "Введите название",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            text = "${name.length}/20",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    nameError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Выбор типа номера
            item {
                Spacer(Modifier.height(8.dp)) // Уменьшено с 16.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Тип",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoomType.entries.forEach { roomType ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (type == roomType.displayName) Color(0xFFF58D4D) else Color(0xFFE0E0E0),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { type = roomType.displayName }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = roomType.displayName,
                                    fontSize = 14.sp,
                                    color = if (type == roomType.displayName) Color.White else Color.Black
                                )
                            }
                        }
                    }

                    typeError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Дополнительные фотографии
            item {
                Spacer(Modifier.height(8.dp)) // Уменьшено с 16.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Дополнительные фотографии",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color.White, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (additionalPhotos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { pickAdditionalImagesLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add_icon),
                                    contentDescription = "Add additional photo",
                                    tint = Color(0xFFF58D4D),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(additionalPhotos.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .height(130.dp)
                                            .aspectRatio(1f)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        if (index < isAdditionalImageLoading.size && isAdditionalImageLoading[index]) {
                                            CircularProgressIndicator(
                                                color = Color(0xFFF58D4D),
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .align(Alignment.Center)
                                            )
                                        } else {
                                            AsyncImage(
                                                model = additionalPhotos[index],
                                                contentDescription = "Additional Image $index",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                onError = { error ->
                                                    Log.e("MakerForRooms", "Failed to load additional image: ${error.result.throwable.message}")
                                                }
                                            )
                                            IconButton(
                                                onClick = {
                                                    additionalPhotos.removeAt(index)
                                                    photoListTrigger++},
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.delete_icon),
                                                    contentDescription = "Delete Additional Image",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (additionalPhotos.size < 9) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .height(130.dp)
                                                .aspectRatio(1f)
                                                .background(Color.White, RoundedCornerShape(8.dp))
                                                .clickable { pickAdditionalImagesLauncher.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.add_icon),
                                                contentDescription = "Add additional photo",
                                                tint = Color(0xFFF58D4D),
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        additionalPhotosError?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        } ?: Spacer(Modifier.weight(1f))

                        Text(
                            text = "${additionalPhotos.size}/9",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
            }

            // Поле для описания
            item {
                Spacer(Modifier.height(8.dp)) // Уменьшено с 16.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)

                ) {
                    Text(
                        text = "Описание",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = description,
                            onValueChange = { if (it.length <= 100) description = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (description.isEmpty()) {
                                    Text(
                                        text = "Введите описание",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Text(
                            text = "${description.length}/100",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    descriptionError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Поле для цены
            item {
                Spacer(Modifier.height(8.dp)) // Уменьшено с 16.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp) // Уменьшаем вертикальный padding

                ) {
                    Text(
                        text = "Цена за одни сутки (от 100 до 10000000 рублей)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = price,
                            onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (price.isEmpty()) {
                                    Text(
                                        text = "Введите значение",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            text = "руб. в день",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    priceError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Поле для вместимости
            item {
                Spacer(Modifier.height(8.dp)) // Уменьшено с 16.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp) // Уменьшаем вертикальный padding

                ) {
                    Text(
                        text = "Вместимость (от 1 до 4 человек)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = capacity,
                            onValueChange = { capacity = it.filter { char -> char.isDigit() } },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (capacity.isEmpty()) {
                                    Text(
                                        text = "Введите значение",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            text = "чел.",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    capacityError?.let { error ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp)) // Уменьшено с 16.dp
            }
        }
    }
}