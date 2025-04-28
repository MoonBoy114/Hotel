package com.example.hotel.detailscreens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel.data.viewmodel.HotelViewModel
import kotlinx.coroutines.launch


@Composable
fun NewsDetailScreen(
    viewModel: HotelViewModel,
    newsId: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Состояние для новости
    var news by remember { mutableStateOf<com.example.hotel.data.News?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Загружаем новость по ID
    LaunchedEffect(newsId) {
        coroutineScope.launch {
            news = viewModel.getNewsById(newsId)
        }
    }

    // Состояние для текущего индекса дополнительной фотографии
    var currentPhotoIndex by remember { mutableStateOf(0) }

    // Если новость не загружена, показываем индикатор загрузки
    if (news == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(top = 25.dp)
        ) {
            // Верхняя панель с кнопкой "Назад"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Подробнее",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Основное изображение с возможностью листать и анимацией
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Используем Crossfade для плавного перехода между изображениями
                    Crossfade(
                        targetState = if (news!!.additionalPhotos.isNotEmpty())
                            news!!.additionalPhotos[currentPhotoIndex]
                        else
                            news!!.imageUrl,
                        animationSpec = tween(durationMillis = 300), // Длительность анимации 300 мс
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    ) { currentImageUrl ->
                        AsyncImage(
                            model = currentImageUrl,
                            contentDescription = "Main Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Стрелки для листания, если есть дополнительные фотографии
                    if (news!!.additionalPhotos.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentPhotoIndex > 0) {
                                        currentPhotoIndex--
                                    } else {
                                        currentPhotoIndex = news!!.additionalPhotos.size - 1
                                    }
                                },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Previous",
                                    tint = Color.Black
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (currentPhotoIndex < news!!.additionalPhotos.size - 1) {
                                        currentPhotoIndex++
                                    } else {
                                        currentPhotoIndex = 0
                                    }
                                },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Next",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Заголовок новости
                Text(
                    text = news!!.title.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(10.dp))



                // Описание новости
                Text(
                    text = news!!.content,
                    fontSize = 18.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}