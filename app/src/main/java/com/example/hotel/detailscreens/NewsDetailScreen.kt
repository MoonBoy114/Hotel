package com.example.hotel.detailscreens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
// Состояние для смещения при свайпе
    var offsetX by remember { mutableStateOf(0f) }
// Анимированное значение смещения
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 600), // Замедляем анимацию до 600 мс
        label = "offsetXAnimation"
    )

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
                .padding(top = 35.dp)
        ) {
            // Верхняя панель с кнопкой "Назад"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(start = 10.dp),
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Основная фотография (над заголовком)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomEnd = 10.dp, bottomStart = 10.dp))
            ) {
                AsyncImage(
                    model = news!!.imageUrl,
                    contentDescription = "Main Photo",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Заголовок новости
                Text(
                    text = news!!.title.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(10.dp))

                // Подзаголовок (если есть)
                news!!.subTitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        fontSize = 18.sp,
                        color = Color(0xFF666666)
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // Дополнительные фотографии (между подзаголовком и описанием или заголовком и описанием)
                if (news!!.additionalPhotos.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.LightGray)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        // Определяем направление свайпа по конечному смещению
                                        if (offsetX < 0) { // Свайп влево
                                            if (currentPhotoIndex < news!!.additionalPhotos.size - 1) {
                                                currentPhotoIndex++
                                            } else {
                                                currentPhotoIndex = 0
                                            }
                                        } else if (offsetX > 0) { // Свайп вправо
                                            if (currentPhotoIndex > 0) {
                                                currentPhotoIndex--
                                            } else {
                                                currentPhotoIndex = news!!.additionalPhotos.size - 1
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
                            targetState = news!!.additionalPhotos[currentPhotoIndex],
                            animationSpec = tween(durationMillis = 600), // Замедляем анимацию до 600 мс
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(animatedOffsetX.toInt(), 0) } // Применяем анимированное смещение
                                .clip(RoundedCornerShape(10.dp))
                        ) { currentImageUrl ->
                            AsyncImage(
                                model = currentImageUrl,
                                contentDescription = "Additional Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Индикаторы (кружочки)
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            news!!.additionalPhotos.forEachIndexed { index, _ ->
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
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Описание новости
                Text(
                    text = news!!.content,
                    fontSize = 18.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }

}