package com.example.hotel.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R

// Модель данных для ресторанов/баров
data class Restaurant(
    val name: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun RestaurantScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    // Список ресторанов и баров
    val restaurants = listOf(
        Restaurant(
            "The Grilled",
            "В ресторане The Grilled ежедневно сервируется завтрак, обед и ужин в формате «Шведский стол».\n\n" +
                    "В ресторане вы можете дополнительно заказать блюда и напитки по меню a la carte. Официанты предложат детское меню для юных гостей отеля.\n\n" +
                    "Расположение: Корпус Modern, 1 этаж\n" +
                    "Кухня: европейская\n\n" +
                    "Расписание «Шведской линии»:\n" +
                    "    Завтрак: 08:00 - 11:00\n" +
                    "    Обед: 13:30 - 16:00\n" +
                    "    Ужин: 18:30 - 21:00\n\n" +
                    "*В случае, если при бронировании вы не оплатили питание по системе \"Шведский стол\", оплата питания возможна на месте. Подробности у сотрудников ресторанного комплекса.",
            R.drawable.restaurant_grilled
        ),
        Restaurant("Lobby bar Mangup", "Комфортно организованное пространство, удобная мебель, внимательное обслуживание и отличный сигнал Wi-Fi – прекрасное место для отдыха. В Баре Вам всегда предложат ароматный кофе и свежие десерты, элитные сорта виски и Крымские вина.\n" +
                "\n" +
                "    Расположение: Корпус Modern, 1 этаж\n" +
                "    Время работы: с 09:00 до 23:00\n", R.drawable.restaurant_lobby),
        Restaurant("Tavrika", "В формате «Шведский стол» ресторан «Tavrika» открыт для гостей отеля.\n" +
                "\n" +
                "В Ресторане вы можете дополнительно заказать блюда и напитки по меню a la carte. Официанты предложат детское меню для юных гостей отеля.\n" +
                "\n" +
                "    Завтрак: 08:00 - 11:00\n" +
                "    Обед: 13:30 - 16:00\n" +
                "    Ужин: 18:30 - 21:00\n" +
                "\n" +
                "Ресторан также идеально подходит для закрытого обеда или ужина, а также для проведения праздничного мероприятия.\n" +
                "\n" +
                "    Кухня: локальная\n" +
                "    Расположение: Корпус Modern", R.drawable.restaurant_tavrika),
        Restaurant("Pool bar",
            "«Pool BAR» расположен в одном из открытых бассейнов аква-зоны отеля. В жаркий летний день Вы сможете насладиться прохладительными напитками, фирменными коктейлями не выходя из бассейна.\n" +
                    "\n" +
                    "    Открыт с мая по сентябрь\n" +
                    "    Расположение: Центральный басейн\n" +
                    "    Время работы: с 09:00 до 18:00\n", R.drawable.restaurant_pool),
        Restaurant("La Veranda",
            "Ресторан «La Veranda» расположен на белокаменной веранде исторического корпуса Классик, откуда открывается прекрасный вид на Черное море и парк отеля.\n" +
                    "Каждый сезон Шеф-повар разрабатывает новое уникальное меню, чтобы удивить гостей изысканными блюдами и необычными сочетаниями.\n" +
                    "В ресторане часто проходят мастер-классы, где Шеф каждый раз удивляет гостей интересными техниками приготовления блюд и еще более мощными и многогранными вкусами.\n" +
                    "\n" +
                    "    Кухня: локальная\n" +
                    "    Открыт с июня по сентябрь\n" +
                    "    Расположение: Корпус Classic\n" +
                    "    Время работы: с 13:00 до 23:00\n", R.drawable.restaurant_veranda),
        Restaurant("Мангал в саду",
            "«Мангал в саду» – это место для ценителей блюд на гриле. Сочным дополнением станут пряности и соусы, которые мы используем в процессе готовки. Взрыв вкуса обеспечен!\n" +
                    "\n" +
                    "    Расположение: реликтовый парк\n" +
                    "    Открыт с мая по сентябрь\n" +
                    "    Время работы: с 12:00 до 17:00\n", R.drawable.restaurant_sea)
    )

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
                    text = "Рестораны",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Start
                )
            }

            items(restaurants) { restaurant ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Image(
                            painter = painterResource(id = restaurant.imageRes),
                            contentDescription = restaurant.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = restaurant.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f).padding(8.dp)
                            )
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                fontSize = 16.sp,
                                color = Color(0xFFF58D4D),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = restaurant.description,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                                Divider(
                                    color = Color(0xFFF58D4D),
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}