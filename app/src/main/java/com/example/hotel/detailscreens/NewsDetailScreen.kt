package com.example.hotel.detailscreens

import android.graphics.Bitmap
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
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
        animationSpec = tween(durationMillis = 600),
        label = "offsetXAnimation"
    )

    // Состояние для доминирующего цвета фона (только для дополнительных фотографий)
    var backgroundColor by remember { mutableStateOf(Color.Transparent) }
    // Состояние для определения, нужно ли применять фон
    var shouldApplyBackground by remember { mutableStateOf(false) }

    // Получаем ширину экрана в пикселях
    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    // Высота контейнера в пикселях (190.dp)
    val containerHeightPx = with(LocalDensity.current) { 190.dp.toPx() }
    // Пропорции контейнера
    val containerAspectRatio = screenWidthPx / containerHeightPx

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
                    text = "Подробнее о новости",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Основная фотография (без фона)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomEnd = 10.dp, bottomStart = 10.dp))
            ) {
                AsyncImage(
                    model = news!!.imageUrl,
                    contentDescription = "Main Photo",
                    modifier = Modifier.fillMaxSize(),
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
                    // Состояние для хранения загруженного битмапа
                    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

                    // Невидимый AsyncImage для получения битмапа и размеров изображения
                    AsyncImage(
                        model = news!!.additionalPhotos[currentPhotoIndex],
                        contentDescription = null,
                        modifier = Modifier
                            .size(1.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Fit,
                        onSuccess = { result ->
                            bitmap = result.result.drawable.toBitmap()
                            // Определяем, нужно ли применять фон
                            bitmap?.let { bmp ->
                                val imageAspectRatio = bmp.width.toFloat() / bmp.height.toFloat()
                                // Если пропорции изображения отличаются от пропорций контейнера,
                                // изображение не заполняет контейнер полностью
                                shouldApplyBackground = imageAspectRatio != containerAspectRatio
                            }
                        },
                        onError = {
                            bitmap = null
                            shouldApplyBackground = false
                        }
                    )

                    // Обновляем доминирующий цвет при смене изображения
                    LaunchedEffect(bitmap) {
                        bitmap?.let { bmp ->
                            if (bmp.width > 0 && bmp.height > 0) {
                                // Используем оригинальный битмап для анализа, не создавая копию
                                Palette.from(bmp).generate { palette ->
                                    backgroundColor = palette?.dominantSwatch?.rgb?.let { Color(it) } ?: Color.Transparent
                                }
                            } else {
                                backgroundColor = Color.Transparent
                            }
                        } ?: run {
                            backgroundColor = Color.Transparent
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (shouldApplyBackground) backgroundColor else Color.Transparent)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
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
                                        offsetX = 0f
                                    },
                                    onDragCancel = {
                                        offsetX = 0f
                                    }
                                ) { _, dragAmount ->
                                    offsetX += dragAmount
                                }
                            }
                    ) {
                        // Основное изображение
                        Crossfade(
                            targetState = news!!.additionalPhotos[currentPhotoIndex],
                            animationSpec = tween(durationMillis = 600),
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(animatedOffsetX.toInt(), 0) }
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
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}