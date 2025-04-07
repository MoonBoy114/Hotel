package com.example.hotel.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hotel.R
import com.example.hotel.data.viewmodel.HotelViewModel


data class Category(val imageRes: Int, val title: String)

@Composable
fun HomeScreen(viewModel: HotelViewModel, modifier: Modifier = Modifier) {
    val currentUser by viewModel.currentUser.observeAsState()
    val userRole = currentUser?.role ?: "Guest"
    LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(top = 22.dp),



        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF58D4D))


                    ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_of_riviera),
                        contentDescription = "Logo",
                        modifier = Modifier.height(50.dp)
                    )


                    Icon(
                        painter = painterResource(id = R.drawable.info_icon),
                        contentDescription = "About",
                        modifier = Modifier.height(40.dp)
                            .padding(start = 370.dp, top = 10.dp)
                            .clickable { },
                        tint = Color.White,


                        )


                }
                Spacer(Modifier.height(60.dp))
            }


            item {
                if (userRole == "Guest") {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ПОКА НЕТ АКЦИЙ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )


                    }
                    Spacer(Modifier.height(80.dp))
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        IconButton(
                            onClick = { }

                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add_icon),
                                contentDescription = "Add",
                                modifier = Modifier.size(100.dp),
                                tint = Color(0xFFF58D4D)
                            )
                        }

                    }
                    Spacer(Modifier.height(15.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center

                    )

                    {
                        Text(
                            text = "ДОБАВЬТЕ СВОЮ ПЕРВУЮ АКЦИЮ!",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,

                            )

                    }
                    Spacer(Modifier.height(40.dp))
                }
            }


            val categories = listOf(
                Category(R.drawable.restoran, "Рестораны"),
                Category(R.drawable.room, "Номера"),
                Category(R.drawable.hotel, "Отели"),
                Category(R.drawable.animations, "Анимации"),
                Category(R.drawable.taxi, "Трансфер"),
                Category(R.drawable.spa_image, "SPA")

            )

            items(categories.chunked(2)) { categoryPair ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    categoryPair.forEach { category ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomEnd = 5.dp, bottomStart = 5.dp))
                                .weight(1f)


                        ) {
                            CategoryItem(
                                imageRes = category.imageRes,
                                title = category.title
                            )
                        }
                    }

                    if (categoryPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }


@Composable
fun CategoryItem(imageRes: Int, title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { }
            .padding(8.dp)
    ) {
        // Изображение
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Фиксированная высота для изображения
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 5.dp, bottomEnd = 5.dp)), // Закругление только сверху
            contentScale = ContentScale.Crop // Масштабирование изображения
        )

        // Текст и иконка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp), // Отступы для текста и иконки
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Icon(
                painter = painterResource(id = R.drawable.bold_right_arrow),
                contentDescription = "Arrow",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFF58D4D)
            )
        }
    }
}