package com.example.hotel.makers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hotel.R
import com.example.hotel.data.News
import com.example.hotel.data.viewmodel.HotelViewModel
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun MakerForNews(
    viewModel: HotelViewModel,
    navController: NavController,
    newsId: String? = null
) {
    var title by remember { mutableStateOf("") }
    var subTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    val additionalPhotos = remember { mutableStateListOf<String>() }
    var isMainImageLoading by remember { mutableStateOf(false) }
    val isAdditionalImageLoading =
        remember { mutableStateListOf<Boolean>().apply { repeat(5) { add(false) } } }

    LaunchedEffect(newsId) {
        if (newsId != null) {
            viewModel.getNewsById(newsId)?.let { news ->
                title = news.title
                subTitle = news.subTitle
                description = news.content
                imageUrl = news.imageUrl
                additionalPhotos.clear()
                additionalPhotos.addAll(news.additionalPhotos)
            }
        }
    }

    val context = LocalContext.current
    val storage = FirebaseStorage.getInstance()

    val pickMainImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { it ->
            isMainImageLoading = true
            val storageRef = storage.reference.child("news_images/${UUID.randomUUID()}.jpg")
            storageRef.putFile(it).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                imageUrl = downloadUri.toString()
                isMainImageLoading = false
            }.addOnFailureListener {
                viewModel.setErrorMessage("Ошибка загрузки основной фотографии: ${it.message}")
                isMainImageLoading = false
            }
        }
    }

    // Исправляем валидацию
    val isFormValid by remember(title, subTitle, description, imageUrl, additionalPhotos) {
        mutableStateOf(
            title.isNotBlank() && title.length <= 10 &&
                    description.isNotBlank() && description.length <= 200 &&
                    imageUrl.isNotBlank() &&
                    (subTitle.isEmpty() || subTitle.length <= 20) &&
                    additionalPhotos.size <= 5
        )
    }

    val pickAdditionalImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEachIndexed { index, uri ->
            if (additionalPhotos.size < 5) {
                val adjustedIndex = additionalPhotos.size
                if (adjustedIndex < isAdditionalImageLoading.size) {
                    isAdditionalImageLoading[adjustedIndex] = true
                }
                val storageRef =
                    storage.reference.child("news_images/additional_${UUID.randomUUID()}.jpg")
                storageRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    storageRef.downloadUrl
                }.addOnSuccessListener { downloadUri ->
                    if (additionalPhotos.size < 5) {
                        additionalPhotos.add(downloadUri.toString())
                    }
                    if (adjustedIndex < isAdditionalImageLoading.size) {
                        isAdditionalImageLoading[adjustedIndex] = false
                    }
                }.addOnFailureListener {
                    viewModel.setErrorMessage("Ошибка загрузки дополнительных фотографий: ${it.message}")
                    if (adjustedIndex < isAdditionalImageLoading.size) {
                        isAdditionalImageLoading[adjustedIndex] = false
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
            .padding(top = 20.dp)
                            ,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(vertical = 16.dp, horizontal = 16.dp)

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.navigate("news") }
                    )

                    Text(
                        text = "Конструктор для новости",
                        fontSize = 20.sp,
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
                    enabled = isFormValid
                ) {
                    Text(
                        text = if (newsId == null) "Добавить" else "Изменить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFormValid) Color.White else Color(0xFF666666)
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
                            .height(150.dp)
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
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    )
                                    IconButton(
                                        onClick = { imageUrl = "" },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
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
                }
            }

            // Поле для заголовка
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = {
                            if (it.length <= 10) title = it
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
                        text = "${title.length}/10",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            // Поле для подзаголовка
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = subTitle,
                        onValueChange = { if (it.length <= 20) subTitle = it },
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
                        text = "${subTitle.length}/20",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
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
                                            .size(80.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
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
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp)
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
                                                .size(80.dp)
                                                .background(Color.LightGray, RoundedCornerShape(8.dp))
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

                    Text(
                        text = "${additionalPhotos.size}/5",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Поле для описания
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = description,
                        onValueChange = { if (it.length <= 200) description = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
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
                Spacer(Modifier.height(16.dp)) // Отступ внизу для прокрутки
            }
        }
    }
}

