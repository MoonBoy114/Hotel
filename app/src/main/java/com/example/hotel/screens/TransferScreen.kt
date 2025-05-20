package com.example.hotel.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R

@Composable
fun TransferScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_of_riviera),
                    contentDescription = "Логотип Riviera",
                    modifier = Modifier
                        .height(30.dp)
                        .align(Alignment.CenterStart)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 38.dp)

            .background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(bottom = 35.dp)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Заголовок
                Text(
                    text = "Трансфер",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Легковой автомобиль с описанием
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_1),
                            contentDescription = "Белый автомобиль",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Легковой автомобиль вместимостью до 4-х мест:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center

                        )
                        Text(
                            text = "Трансфер в любом направлении ",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("50 ₽ (1км) – в одну сторону")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(" 50 ₽")
                                }
                            },
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Трансфер в обратном направлении ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("25 ₽ (1 км)")
                                }
                            },
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Простой ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("1000 ₽ (1 час)")
                                }
                            },
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Минивэн с описанием
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_2),
                            contentDescription = "Желтый минивэн",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Минивэн вместимостью до 8-ми мест:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Трансфер в любом направлении ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("80 ₽ (1км) – в одну сторону")
                                }
                            },
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Трансфер в обратном направлении ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("40 ₽ (1 км)")
                                }
                            },
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Простой ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("1 500 ₽ (1 час)")
                                }
                            },
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Маршрут 1: Симферополь
                TransferRoute(
                    imageRes = R.drawable.img_3,
                    title = "Из/в ж/д вокзал Симферополя",
                    priceCar = "Трансфер ж/д вокзал Симферополя (легковой) – 3 500 ₽",
                    priceVan = "Трансфер ж/д вокзал Симферополя (минивэн) – 5 500 ₽"
                )

                // Маршрут 2: Севастополь
                TransferRoute(
                    imageRes = R.drawable.img_4,
                    title = "Из/в ж/д вокзал Севастополя",
                    priceCar = "Трансфер ж/д вокзал Севастополя (легковой) – 3 000 ₽",
                    priceVan = "Трансфер ж/д вокзал Севастополя (минивэн) – 5 000 ₽"
                )

                // Маршрут 3: Керчь
                TransferRoute(
                    imageRes = R.drawable.img_5,
                    title = "Из/в ж/д вокзал Керчи",
                    priceCar = "Трансфер ж/д вокзал Керчи (легковой) – 5 000 ₽",
                    priceVan = "Трансфер ж/д вокзал Керчи (минивэн) – 7 000 ₽"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Проверка отображения строки
                Text(
                    text = buildAnnotatedString {
                        append("Заказать можно по телефону ")
                        withStyle(style = SpanStyle(color = Color(0xFFF58D4D), fontWeight = FontWeight.Bold)) {
                            append("3552")
                        }
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()

                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )



                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun TransferRoute(imageRes: Int, title: String, priceCar: String, priceVan: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(vertical = 8.dp)
        )
        Text(
            text = buildAnnotatedString {
                append("Трансфер ж/д вокзал ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(priceCar.replace("Трансфер ж/д вокзал ", "").replace(" (легковой)", ""))
                }
                append(" (легковой)")
            },
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = buildAnnotatedString {
                append("Трансфер ж/д вокзал ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(priceVan.replace("Трансфер ж/д вокзал ", "").replace(" (минивэн)", ""))
                }
                append(" (минивэн)")
            },
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
    }
}