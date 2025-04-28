package com.example.hotel.screens

import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotel.R
import com.example.hotel.data.News
import com.example.hotel.data.viewmodel.HotelViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: HotelViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val searchQuery = remember { mutableStateOf("") }
    val currentUser by viewModel.currentUser.observeAsState()
    val userRole = currentUser?.role ?: "Guest"
    val newsList by viewModel.news.observeAsState(emptyList())

    // Локальное состояние для анимации удаления
    val visibleNews = remember { mutableStateListOf<News>() }
    LaunchedEffect(newsList) {
        visibleNews.clear()
        visibleNews.addAll(newsList)
    }

    // Для логирования ошибок
    val errorMessage by viewModel.errorMessage.observeAsState()
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Log.e("NewsScreen", message)
            viewModel.clearError()
        }
    }

    // Состояние для диалога подтверждения удаления
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newsToDelete by remember { mutableStateOf<News?>(null) }

    // Загружаем новости при открытии экрана
    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    // Фильтрация новостей на основе поискового запроса
    val filteredNews by remember(searchQuery.value, visibleNews) {
        derivedStateOf {
            if (searchQuery.value.isBlank()) {
                visibleNews.toList()
            } else {
                val query = searchQuery.value.lowercase()
                visibleNews.filter { news ->
                    news.title.lowercase().contains(query) ||
                            news.subTitle.lowercase().contains(query) ||
                            news.content.lowercase().contains(query)
                }
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

        // Находим все вхождения запроса в текст
        while (index >= 0) {
            indices.add(index)
            index = lowerText.indexOf(lowerQuery, index + 1)
        }

        // Если совпадений нет, возвращаем исходный текст
        if (indices.isEmpty()) {
            return AnnotatedString(text)
        }

        // Создаём AnnotatedString с подсветкой
        return buildAnnotatedString {
            var lastIndex = 0
            for (i in indices) {
                // Добавляем текст до совпадения
                if (lastIndex < i) {
                    append(text.substring(lastIndex, i))
                }
                // Добавляем совпадение с жёлтым фоном
                val start = i
                val end = i + query.length
                pushStyle(SpanStyle(background = Color.Yellow))
                append(text.substring(start, end))
                pop()
                lastIndex = end
            }
            // Добавляем оставшийся текст после последнего совпадения
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog && newsToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Удалить новость?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить новость \"${newsToDelete!!.title}\"?",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        visibleNews.remove(newsToDelete)
                        viewModel.deleteNews(newsToDelete!!.newsId)
                        showDeleteDialog = false
                        newsToDelete = null
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
                        newsToDelete = null
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

    // Основной контейнер
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))

            .padding(top = 5.dp)
    ) {
        // Поисковая строка (фиксированная)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF58D4D))
                .padding(8.dp)
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
                            text = "Поиск новостей...",
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
        }

        // Заголовок "Новости" и кнопка добавления (фиксированная)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Новости",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp)
            )

            if (userRole != "Guest") {
                IconButton(
                    onClick = { navController.navigate("makerForNews") },
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_icon),
                        contentDescription = "Add News",
                        tint = Color(0xFFF58D4D),
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }

        // Список новостей (скроллируемая часть)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
        ) {
            if (filteredNews.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 250.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (searchQuery.value.isBlank()) "ПОКА НЕТ НОВОСТЕЙ" else "НОВОСТЕЙ НЕ НАЙДЕНО",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(80.dp))
                }
            } else {
                itemsIndexed(filteredNews, key = { _, news -> news.newsId }) { _, news ->
                    AnimatedVisibility(
                        visible = news in visibleNews,
                        exit = slideOutVertically(
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 300)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(Color.White, RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Контейнер для изображений
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        ) {
                                            AsyncImage(
                                                model = news.imageUrl,
                                                contentDescription = "Main Image",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(topStart = 8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        if (news.additionalPhotos.isNotEmpty()) {
                                            if (news.additionalPhotos.size == 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                ) {
                                                    AsyncImage(
                                                        model = news.additionalPhotos[0],
                                                        contentDescription = "Additional Image 1",
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(RoundedCornerShape(topEnd = 8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            } else {
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                ) {
                                                    AsyncImage(
                                                        model = news.additionalPhotos[0],
                                                        contentDescription = "Additional Image 1",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(topEnd = 8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    if (news.additionalPhotos.size > 1) {
                                                        AsyncImage(
                                                            model = news.additionalPhotos[1],
                                                            contentDescription = "Additional Image 2",
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .weight(1f),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                            )
                                        }
                                    }

                                    if (userRole != "Guest") {
                                        var showMenu by remember { mutableStateOf(false) }

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
                                                        navController.navigate("makerForNews/${news.newsId}")
                                                        showMenu = false
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
                                                        newsToDelete = news
                                                        showDeleteDialog = true
                                                        showMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Заголовок с подсветкой совпадений
                                Text(
                                    text = highlightText(news.title.uppercase(), searchQuery.value),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                Spacer(Modifier.height(8.dp))

                                // Подзаголовок или описание с подсветкой совпадений
                                Text(
                                    text = highlightText(
                                        if (news.subTitle.isEmpty()) news.content else news.subTitle,
                                        searchQuery.value
                                    ),
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Кнопка "Подробнее"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp, end = 16.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clickable { navController.navigate("newsDetail/${news.newsId}") }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Подробнее",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF58D4D)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "More",
                                            tint = Color(0xFFF58D4D),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
