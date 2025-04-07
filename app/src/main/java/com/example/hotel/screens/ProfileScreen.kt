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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R
import com.example.hotel.data.viewmodel.HotelViewModel



var money: Double = 1000.0
@Composable
fun ProfileScreen(viewModel: HotelViewModel, navController: NavHostController, modifier: Modifier = Modifier) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Профиль", style = MaterialTheme.typography.headlineMedium)
//        Spacer(modifier = Modifier.height(10.dp))
//        Button(onClick = {
//            viewModel.logout()
//            navController.navigate("login") {
//                popUpTo(navController.graph.startDestinationId) { inclusive = true }
//            }
//        }) {
//            Text("Выйти")
//        }
//    }
    val currentUser by viewModel.currentUser.observeAsState()

    val currentName = currentUser?.name


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
            Spacer(Modifier.height(20.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {

                    },
                    modifier = Modifier.size(140.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_profile),
                        contentDescription = "profile icon",
                        tint = Color(0xFFF58D4D)

                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (currentName != null) {
                    Text(
                        text = currentName,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape( // Фон со скруглением
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomEnd = 10.dp,
                        bottomStart = 10.dp
                    ))
                    .clickable {

                    },
                verticalAlignment = Alignment.CenterVertically



            ) {
                    Image(
                        painter = painterResource(id = R.drawable.small_profile),
                        contentDescription = "small profile",
                        modifier = Modifier.padding(8.dp).size(30.dp)
                    )

                    Text(
                    text = "Мои данные",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold

                    )
                    IconButton(
                        onClick = {

                        },
                        modifier = Modifier.padding(start = 190.dp).size(25.dp)
                    ) {
                       Icon(
                           painter = painterResource(id = R.drawable.right_arrow),
                           contentDescription = "right arrow",
                           tint = Color(0xFFF58D4D)

                       )
                    }


            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(10.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    // Первая строка: "RS Кошелёк" и стрелка
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RS Кошелёк",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.right_arrow),
                            contentDescription = "right arrow",
                            tint = Color(0xFFF58D4D),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(16.dp)
                        )
                    }

                    // Вторая строка: сумма
                    Text(
                        text = "${money.toInt()} ₽",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }


                Button(
                    onClick = {  },
                    modifier = Modifier
                        .height(36.dp)
                        .padding(end = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58D4D)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Пополнить",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }


        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Ваши брони",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Тут отобразятся бронирования пользователя",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Spacer для визуального разделения
        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
            ) {
                // Настройки
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.settings_profile),
                        contentDescription = "settings icon",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(30.dp)
                    )

                    Text(
                        text = "Настройки",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.right_arrow),
                        contentDescription = "right arrow",
                        tint = Color(0xFFF58D4D),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(16.dp)
                    )
                }

                // Разделитель
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFFE0E0E0)) // Серый цвет для разделителя
                )

                // Поддержка
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.support_img),
                        contentDescription = "support icon",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(30.dp)
                    )

                    Text(
                        text = "Поддержка",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.right_arrow),
                        contentDescription = "right arrow",
                        tint = Color(0xFFF58D4D),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(16.dp)
                    )
                }
            }
        }

        // Spacer для визуального разделения
        item {
            Spacer(Modifier.height(16.dp))
        }


    }
}