package com.example.hotel.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R

@Composable
fun SPAScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val phoneNumber = "+79789153591"

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box() {
                        Image(
                            painter = painterResource(id = R.drawable.logo_of_riviera),
                            contentDescription = "Логотип Riviera",
                            modifier = Modifier
                                .height(30.dp)
                                .align(Alignment.CenterStart)
                        )
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SPA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Start
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpaCard(
                        title = "Локации",
                        imageRes = R.drawable.locations_spa
                    )
                    SpaCard(
                        title = "SPA-Меню",
                        imageRes = R.drawable.menu_spa
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpaCard(
                        title = "Услуги",
                        imageRes = R.drawable.services_spa
                    )
                    SpaCard(
                        title = "SPA-Программы",
                        imageRes = R.drawable.programs_spa
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Text(
                    text = "Время работы:\n" +
                            "с 9:00 до 21:00\n" +
                            "с 9:00 до 19:00 - для гостей до 12 лет\n" +
                            "Работа локаций для отдыха в отеле:\n" +
                            "Открытый бассейн с подогревом работает с 09:00 до 18:00\n" +
                            "Крытый бассейн - работает с 09:00 до 18:00\n" +
                            "Сауна - воскресенье с 09:00 до 18:00",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ПОЗВОНИТЬ В SPA",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "НАПИСАТЬ В SPA",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }


        }
    }
}

@Composable
fun SpaCard(title: String, imageRes: Int) {
    Column(
        modifier = Modifier
            .clickable { /* Логика клика */ }

    ) {

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier
                .size(150.dp, 120.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 5.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }
}