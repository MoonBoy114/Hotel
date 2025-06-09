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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hotel.HotelApp
import com.example.hotel.R
import com.example.hotel.data.News
import com.example.hotel.data.viewmodel.HotelViewModel
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MakerForNews(
    viewModel: HotelViewModel,
    navController: NavController,
    newsId: String? = null
) {
    // Текущие значения полей
    var title by remember { mutableStateOf("") }
    var subTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    val additionalPhotos = remember { mutableStateListOf<String>() }

    // Исходные значения полей (для сравнения изменений)
    var originalTitle by remember { mutableStateOf("") }
    var originalSubTitle by remember { mutableStateOf("") }
    var originalDescription by remember { mutableStateOf("") }
    var originalImageUrl by remember { mutableStateOf("") }
    val originalAdditionalPhotos = remember { mutableStateListOf<String>() }

    var isMainImageLoading by remember { mutableStateOf(false) }
    val isAdditionalImageLoading =
        remember { mutableStateListOf<Boolean>().apply { repeat(5) { add(false) } } }

    // Состояния для сообщений об ошибках
    var titleError by remember { mutableStateOf<String?>(null) }
    var subTitleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var imageUrlError by remember { mutableStateOf<String?>(null) }
    var additionalPhotosError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val app = context.applicationContext as HotelApp
    val storage = HotelApp.storage
    val coroutineScope = rememberCoroutineScope()

    // Загружаем новость и сохраняем исходные значения
    LaunchedEffect(newsId) {
        if (newsId != null) {
            viewModel.getNewsById(newsId)?.let { news ->
                title = news.title
                subTitle = news.subTitle
                description = news.content
                imageUrl = news.imageUrl
                additionalPhotos.clear()
                additionalPhotos.addAll(news.additionalPhotos)

                // Сохраняем исходные значения
                originalTitle = news.title
                originalSubTitle = news.subTitle
                originalDescription = news.content
                originalImageUrl = news.imageUrl
                originalAdditionalPhotos.clear()
                originalAdditionalPhotos.addAll(news.additionalPhotos)
            }
        }
    }

    val pickMainImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            Log.d("MakerForNews", "Uri is null, image selection cancelled")
            imageUrlError = "Изображение не выбрано"
            return@rememberLauncherForActivityResult
        }
        Log.d("MakerForNews", "Selected Uri: $uri")
        isMainImageLoading = true

        coroutineScope.launch {
            try {
                val fileId = UUID.randomUUID().toString()
                Log.d("MakerForNews", "Generated fileId: $fileId")

                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                Log.d("MakerForNews", "Detected MIME type: $mimeType")

                val extension = when (mimeType) {
                    "image/jpeg", "image/jpg" -> "jpg"
                    "image/png" -> "png"
                    "image/gif" -> "gif"
                    else -> {
                        Log.w("MakerForNews", "Unsupported MIME type: $mimeType, defaulting to jpg")
                        "jpg"
                    }
                }

                var fileName = uri.lastPathSegment ?: "image_$fileId"
                Log.d("MakerForNews", "Original file name: $fileName")
                if (!fileName.contains(".")) {
                    fileName = "$fileName.$extension"
                } else {
                    val currentExtension = fileName.substringAfterLast(".").lowercase()
                    if (currentExtension != extension) {
                        fileName = fileName.substringBeforeLast(".") + ".$extension"
                    }
                }
                Log.d("MakerForNews", "Final file name: $fileName")

                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                Log.d("MakerForNews", "File bytes read, size: ${bytes?.size ?: 0}")

                val file = bytes?.let {
                    io.appwrite.models.InputFile.fromBytes(
                        bytes = it,
                        filename = fileName,
                        mimeType = mimeType,
                    )
                } ?: throw IllegalStateException("Не удалось прочитать файл")
                Log.d("MakerForNews", "InputFile created")

                val result = storage.createFile(
                    bucketId = "images",
                    fileId = fileId,
                    file = file
                )
                Log.d("MakerForNews", "File uploaded, result: $result")

                val fileUrl = "${HotelApp.client.endpoint}/storage/buckets/images/files/$fileId/view?project=${HotelApp.projectId}"
                imageUrl = fileUrl
                Log.d("MakerForNews", "Image uploaded successfully, URL: $fileUrl")
                isMainImageLoading = false
            } catch (e: AppwriteException) {
                Log.e("MakerForNews", "Failed to upload main image: ${e.message}, code: ${e.code}")
                viewModel.setErrorMessage("Ошибка загрузки основной фотографии: ${e.message}")
                imageUrlError = "Ошибка загрузки: ${e.message}"
                isMainImageLoading = false
            } catch (e: Exception) {
                Log.e("MakerForNews", "Unexpected error during main image upload: ${e.message}")
                viewModel.setErrorMessage("Неожиданная ошибка при загрузке: ${e.message}")
                imageUrlError = "Неожиданная ошибка: ${e.message}"
                isMainImageLoading = false
            }
        }
    }

    // Валидация формы с обновлением сообщений об ошибках
    val isFormValid by remember(title, subTitle, description, imageUrl, additionalPhotos) {
        titleError = null
        subTitleError = null
        descriptionError = null
        imageUrlError = null
        additionalPhotosError = null

        val titleValid = title.isNotBlank() && title.length <= 50
        if (!title.isNotBlank()) {
            titleError = "Заголовок обязателен"
        } else if (title.length > 50) {
            titleError = "Заголовок не должен превышать 50 символов"
        }

        val subTitleValid = subTitle.isEmpty() || subTitle.length <= 60
        if (subTitle.isNotEmpty() && subTitle.length > 60) {
            subTitleError = "Подзаголовок не должен превышать 60 символов"
        }

        val descriptionValid = description.isNotBlank() && description.length <= 200
        if (!description.isNotBlank()) {
            descriptionError = "Описание обязательно"
        } else if (description.length > 200) {
            descriptionError = "Описание не должно превышать 200 символов"
        }

        val imageUrlValid = imageUrl.isNotBlank()
        if (!imageUrlValid && !isMainImageLoading) {
            imageUrlError = "Основная фотография обязательна"
        }

        val additionalPhotosValid = additionalPhotos.size <= 5
        if (!additionalPhotosValid) {
            additionalPhotosError = "Не более 5 дополнительных фотографий"
        }

        mutableStateOf(
            titleValid && subTitleValid && descriptionValid && imageUrlValid && additionalPhotosValid
        )
    }

    // Проверяем, были ли внесены изменения
    val hasChanges by derivedStateOf {
        title != originalTitle ||
                subTitle != originalSubTitle ||
                description != originalDescription ||
                imageUrl != originalImageUrl ||
                additionalPhotos.toList() != originalAdditionalPhotos.toList()
    }

    val pickAdditionalImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) {
            Log.d("MakerForNews", "No additional images selected")
            additionalPhotosError = "Не выбрано ни одного изображения"
            return@rememberLauncherForActivityResult
        }
        Log.d("MakerForNews", "Selected additional images: $uris")
        uris.forEach { uri ->
            if (additionalPhotos.size < 5) {
                isAdditionalImageLoading.add(true)
                val adjustedIndex = isAdditionalImageLoading.size - 1

                coroutineScope.launch {
                    try {
                        val fileId = UUID.randomUUID().toString()
                        Log.d("MakerForNews", "Generated fileId for additional image: $fileId")

                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        Log.d("MakerForNews", "Detected MIME type for additional image: $mimeType")

                        val extension = when (mimeType) {
                            "image/jpeg", "image/jpg" -> "jpg"
                            "image/png" -> "png"
                            "image/gif" -> "gif"
                            else -> {
                                Log.w("MakerForNews", "Unsupported MIME type: $mimeType, defaulting to jpg")
                                "jpg"
                            }
                        }

                        var fileName = uri.lastPathSegment ?: "additional_image_$fileId"
                        Log.d("MakerForNews", "Original additional image file name: $fileName")
                        if (!fileName.contains(".")) {
                            fileName = "$fileName.$extension"
                        } else {
                            val currentExtension = fileName.substringAfterLast(".").lowercase()
                            if (currentExtension != extension) {
                                fileName = fileName.substringBeforeLast(".") + ".$extension"
                            }
                        }
                        Log.d("MakerForNews", "Final additional image file name: $fileName")

                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        Log.d("MakerForNews", "Additional image bytes read, size: ${bytes?.size ?: 0}")

                        val file = bytes?.let {
                            io.appwrite.models.InputFile.fromBytes(
                                bytes = it,
                                filename = fileName,
                                mimeType = mimeType,
                            )
                        } ?: throw IllegalStateException("Не удалось прочитать файл")
                        Log.d("MakerForNews", "InputFile created for additional image")

                        val result = storage.createFile(
                            bucketId = "images",
                            fileId = fileId,
                            file = file
                        )
                        Log.d("MakerForNews", "Additional image uploaded, result: $result")

                        val fileUrl = "${HotelApp.client.endpoint}/storage/buckets/images/files/$fileId/view?project=${HotelApp.projectId}"
                        additionalPhotos.add(fileUrl)
                        Log.d("MakerForNews", "Additional image uploaded: $fileUrl")
                        isAdditionalImageLoading[adjustedIndex] = false
                    } catch (e: AppwriteException) {
                        Log.e("MakerForNews", "Failed to upload additional image: ${e.message}, code: ${e.code}")
                        viewModel.setErrorMessage("Ошибка загрузки дополнительной фотографии: ${e.message}")
                        additionalPhotosError = "Ошибка загрузки: ${e.message}"
                        isAdditionalImageLoading[adjustedIndex] = false
                    } catch (e: Exception) {
                        Log.e("MakerForNews", "Unexpected error during additional image upload: ${e.message}")
                        viewModel.setErrorMessage("Неожиданная ошибка при загрузке: ${e.message}")
                        additionalPhotosError = "Неожиданная ошибка: ${e.message}"
                        isAdditionalImageLoading[adjustedIndex] = false
                    }
                }
            } else {
                Log.d("MakerForNews", "Cannot add more than 5 additional photos")
                additionalPhotosError = "Нельзя добавить больше 5 фотографий"
            }
        }
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
                            .clickable { navController.navigate("news") }
                    )

                    Text(
                        text = "Конструктор для новостей",
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
                        val news = News(
                            newsId = newsId ?: "",
                            title = title,
                            subTitle = subTitle,
                            content = description,
                            imageUrl = imageUrl,
                            additionalPhotos = additionalPhotos
                        )
                        if (newsId == null) {
                            viewModel.addNews(news)
                        } else {
                            viewModel.updateNews(news)
                        }
                        viewModel.loadNews()
                        navController.navigate("news")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF58D4D),
                        disabledContainerColor = Color(0xFFB0B0B0)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    // Кнопка активна, если форма валидна, а для режима редактирования ещё и есть изменения
                    enabled = if (newsId == null) isFormValid else (isFormValid && hasChanges)
                ) {
                    Text(
                        text = if (newsId == null) "Добавить" else "Изменить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (newsId == null) {
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
                                            Log.e("MakerForNews", "Failed to load image: ${error.result.throwable.message}")
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

            // Поле для заголовка
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = {
                                if (it.length <= 50) title = it
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (title.isEmpty()) {
                                    Text(
                                        text = "Заголовок (обязательно)...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            text = "${title.length}/50",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    titleError?.let { error ->
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
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = subTitle,
                            onValueChange = { if (it.length <= 60) subTitle = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (subTitle.isEmpty()) {
                                    Text(
                                        text = "Подзаголовок (необязательно)...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            text = "${subTitle.length}/60",
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

            // Дополнительные фотографии
            item {
                Spacer(Modifier.height(16.dp))
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
                                                    Log.e("MakerForNews", "Failed to load additional image: ${error.result.throwable.message}")
                                                }
                                            )
                                            IconButton(
                                                onClick = { additionalPhotos.removeAt(index) },
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
                                if (additionalPhotos.size < 5) {
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
                            text = "${additionalPhotos.size}/5",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
            }

            // Поле для описания
            item {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = description,
                            onValueChange = { if (it.length <= 200) description = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            decorationBox = { innerTextField ->
                                if (description.isEmpty()) {
                                    Text(
                                        text = "Описание (обязательно)...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Text(
                            text = "${description.length}/200",
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
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}