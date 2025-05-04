package com.example.hotel.makers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.example.hotel.data.Service
import com.example.hotel.data.viewmodel.HotelViewModel
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakerForService(
    viewModel: HotelViewModel,
    navController: NavHostController,
    serviceId: String? = null,
    modifier: Modifier = Modifier
) {
    // Текущие значения полей
    var name by remember { mutableStateOf("") }
    var subTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Исходные значения полей (для сравнения изменений)
    var originalName by remember { mutableStateOf("") }
    var originalSubTitle by remember { mutableStateOf("") }
    var originalDescription by remember { mutableStateOf("") }
    var originalImageUrl by remember { mutableStateOf("") }

    var isMainImageLoading by remember { mutableStateOf(false) }

    // Состояния для сообщений об ошибках
    var nameError by remember { mutableStateOf<String?>(null) }
    var subTitleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var imageUrlError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val app = context.applicationContext as HotelApp
    val storage = HotelApp.storage
    val coroutineScope = rememberCoroutineScope()

    // LazyListState для управления прокруткой
    val lazyListState = rememberLazyListState()

    // FocusRequester для каждого поля
    val nameFocusRequester = remember { FocusRequester() }
    val subTitleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    // Загружаем данные сервиса, если передан serviceId
    LaunchedEffect(serviceId) {
        if (serviceId != null) {
            viewModel.services.value?.find { it.serviceId == serviceId }?.let { service ->
                name = service.name
                subTitle = service.subTitle
                description = service.description
                imageUrl = service.imageUrl

                // Сохраняем исходные значения
                originalName = service.name
                originalSubTitle = service.subTitle
                originalDescription = service.description
                originalImageUrl = service.imageUrl
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

    // Валидация формы
    val isFormValid by remember(name, subTitle, description, imageUrl) {
        nameError = null
        subTitleError = null
        descriptionError = null
        imageUrlError = null

        val nameValid = name.isNotBlank() && name.length <= 50
        if (!name.isNotBlank()) {
            nameError = "Название обязательно"
        } else if (name.length > 50) {
            nameError = "Название не должно превышать 50 символов"
        }

        val subTitleValid = subTitle.length <= 20
        if (subTitle.length > 20) {
            subTitleError = "Подзаголовок не должен превышать 20 символов"
        }

        val descriptionValid = description.isNotBlank() && description.length <= 500
        if (!description.isNotBlank()) {
            descriptionError = "Описание обязательно"
        } else if (description.length > 500) {
            descriptionError = "Описание не должно превышать 500 символов"
        }

        val imageUrlValid = imageUrl.isNotBlank()
        if (!imageUrlValid && !isMainImageLoading) {
            imageUrlError = "Основная фотография обязательна"
        }

        mutableStateOf(nameValid && subTitleValid && descriptionValid && imageUrlValid)
    }

    val hasChanges by remember(name, subTitle, description, imageUrl) {
        mutableStateOf(
            name != originalName ||
                    subTitle != originalSubTitle ||
                    description != originalDescription ||
                    imageUrl != originalImageUrl
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
                        text = "Конструктор для акций",
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
                        val service = Service(
                            serviceId = serviceId ?: "",
                            name = name,
                            subTitle = subTitle,
                            description = description,
                            imageUrl = imageUrl
                        )
                        if (serviceId == null) {
                            viewModel.insertService(service)
                        } else {
                            viewModel.updateService(service)
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
                    enabled = if (serviceId == null) isFormValid else (isFormValid && hasChanges)
                ) {
                    Text(
                        text = if (serviceId == null) "Добавить" else "Изменить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (serviceId == null) {
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
                .padding(paddingValues),
            state = lazyListState
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
                            onValueChange = { if (it.length <= 50) name = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(nameFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(index = 1)
                                        }
                                    }
                                },
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
                            text = "${name.length}/50",
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

            // Поле для подзаголовка
            item {
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Подзаголовок",
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
                            value = subTitle,
                            onValueChange = { if (it.length <= 20) subTitle = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(subTitleFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(index = 2)
                                        }
                                    }
                                },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (subTitle.isEmpty()) {
                                    Text(
                                        text = "Введите подзаголовок",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            text = "${subTitle.length}/20",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    subTitleError?.let { error ->
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

            // Поле для описания (заменено на TextField)
            item {
                Spacer(Modifier.height(8.dp))
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
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 500) description = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .focusRequester(descriptionFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(index = 3, scrollOffset = -100)
                                        }
                                    }
                                },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.Black
                            ),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            placeholder = {
                                Text(
                                    text = "Введите описание",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                            },
                            singleLine = false,
                            maxLines = 5 // Ограничение количества строк для видимости
                        )

                        Text(
                            text = "${description.length}/500",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(start = 8.dp)
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

            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}