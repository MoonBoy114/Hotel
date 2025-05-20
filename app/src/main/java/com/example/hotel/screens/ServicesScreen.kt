package com.example.hotel.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R

data class ServiceItem(
    val id: Int,
    val title: String,
    val iconRes: Int,
    val description: String? = null,
    val images: List<Int>? = null
)

@Composable
fun ServicesScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val services = listOf(
        ServiceItem(
            id = 1,
            title = "Клининг",
            iconRes = R.drawable.ic_cleaning,
            description = "Нам важно обеспечить комфорт пребывания в отеле для каждого гостя, поэтому мы проводим ежедневную уборку номеров, современные средства и стандарты. Поддерживаем чистоту косметические принадлежности. Мы с радостью предоставим нашему гостям дополнительный уборку по вашему запросу. Для этого, пожалуйста, обратитесь к нам по телефону 3552.",
            images = listOf(R.drawable.cleaning_image)
        ),
        ServiceItem(
            id = 2,
            title = "TV",
            iconRes = R.drawable.ic_tv_right,
            description = "Инструкция по использованию пульта управления TV:\n\n" +
                    "1. Кнопка включения/выключения\n" +
                    "2. Кнопки выбора телевизионных программ 0-9, числовые кнопки\n" +
                    "3. Кнопка управления за курсором (вверх ▲ и вниз ▼, влево ◄ и вправо ►)\n" +
                    "4. Кнопка подтверждения выбора 'OK'\n" +
                    "5. Менюшка Радио канала (+/-)\n" +
                    "6. Предыдущий TB канал (-)\n" +
                    "7. Текущий TB канал (+)\n" +
                    "8. Список субтитров дорожки\n" +
                    "9. Телетекст\n" +
                    "10. Субтитры\n" +
                    "11. Открытие экрана (4:3 / 16:9)\n" +
                    "12. Формат картинки\n" +
                    "13. Установка будильника\n" +
                    "14. Таймер сна\n" +
                    "15. Регулятор картинки\n" +
                    "16. Уменьшение громкости\n" +
                    "17. Увеличение громкости\n" +
                    "18. Спецкнопки TB канала\n" +
                    "19. Кнопка «назад»\n" +
                    "20. Уникальное предложение символа\n" +
                    "21. Индикатор нажатия пульта",
            images = listOf(R.drawable.tv_remote_image)
        ),
        ServiceItem(
            id = 3,
            title = "Wi-Fi",
            iconRes = R.drawable.ic_wi_fi,
            description = "Инструкция по подключению к Wi-Fi Riviera Sunrise:\n\n" +
                    "1. Выберите Riviera Sunrise из списка доступных сетей, используя указанную Wi-Fi вашей устройство\n\n" +
                    "2. Нажмите кнопку «Подключиться» или «Connect»\n\n" +
                    "3. Откройте Ваш интернет-браузер (Opera, Google Chrome, Yandex Browser, Internet Explorer) и введите в поисковую строку www.google.com или www.yandex.ru. Откроется окно авторизации интернета.\n\n" +
                    "4. Страница авторизации. Пожалуйста, введите Ваши фамилию, номер комнаты и согласитесь с правилами пользования. После авторизации поля, пожалуйста, нажмите кнопку Connect.\n\n" +
                    "Добро пожаловать в интернет-пространство #RivieraSunrise)",
            images = listOf(R.drawable.wifi_image_1, R.drawable.wifi_image_2, R.drawable.wifi_image_3)
        ),
        ServiceItem(
            id = 4,
            title = "Сейф",
            iconRes = R.drawable.ic_safe_right,
            description = "Инструкция по использованию сейфа:\n\n" +
                    "1. При открытом сейфе: закройте дверцу, наберите комбинацию из четырёх цифр на электронном приборе, затем закройте замок, повернув ручку.\n" +
                    "2. Для открытия сейфа: используйте Ваш код, наберите ту же комбинацию цифр на клавиатуре, откройте дверцу.\n" +
                    "3. Просьба запоминать код, не передавайте его другому лицу!",
            images = listOf(R.drawable.safe_image_right)
        ),
        ServiceItem(
            id = 5,
            title = "Климат-контроль",
            iconRes = R.drawable.ic_climate,
            description = "Действия для управления температурой в помещении и поддержка характера управления (нагрев или охлаждение)\n\n" +
                    "По центру показан текущий режим управления и скорость работы вентилятора\n\n" +
                    "Кнопки для регулирования уставки температуры в помещении\n\n" +
                    "• При однократном нажатии одной из кнопок на ЖК-экране изменяется температура на 0,5°C\n" +
                    "• При длительном нажатии кнопок - или +, значение изменяется быстрее\n\n" +
                    "Кнопки для регулирования уставки температуры в помещении\n\n" +
                    "• При однократном нажатии кнопки «Auto» включается автоматический режим работы\n" +
                    "• При однократном нажатии кнопки «Ручной режим, 1 скорость» включается ручной режим с первой скоростью\n" +
                    "• При однократном нажатии кнопки «Ручной режим, 2 скорости» включается ручной режим со второй скоростью\n" +
                    "• При однократном нажатии кнопки «Ручной режим, 3 скорости» включается ручной режим с третьей скоростью\n\n" +
                    "Кнопки для регулирования уставки температуры в помещении\n\n" +
                    "• При однократном нажатии кнопки «Охлаждение» включается режим охлаждения\n" +
                    "• При однократном нажатии кнопки «Отопление» включается режим нагрева",
            images = listOf(R.drawable.climate_control_image)
        ),
        ServiceItem(
            id = 6,
            title = "Телефоны",
            iconRes = R.drawable.ic_phone,
            description = "Из комнаты в комнату: 8+0+номер комнаты\n" +
                    "Room to room: 8+0+room number\n\n" +
                    "Междугородные звонки: -\n" +
                    "Local calls: -\n\n" +
                    "Международные звонки: -\n" +
                    "Local Calls: -\n\n" +
                    "3553 — S.O.S\n" +
                    "3552 — reception Modern\n" +
                    "3554 — reception Classic\n" +
                    "7777 — room service\n" +
                    "3555 — SPA",
            images = listOf(R.drawable.phone_image)
        ),
        ServiceItem(
            id = 7,
            title = "Карты доступа",
            iconRes = R.drawable.ic_card,
            description = "Как пользоваться магнитной картой:\n\n" +
                    "1. Вставить карту\n" +
                    "2. Достать карту\n" +
                    "3. Нажать на кнопку",
            images = listOf(R.drawable.access_card_image_1, R.drawable.access_card_image_2, R.drawable.access_card_image_3)
        ),
        ServiceItem(
            id = 8,
            title = "Правила в номере",
            iconRes = R.drawable.ic_rules,
            description = "Правила проживания и пользования гостиничными услугами в отеле Riviera Sunrise Resort & SPA\n\nОзнакомиться с правилами",
            images = listOf(R.drawable.hotel_right_image)
        )
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF58D4D))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Сервисы",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .padding(top = 38.dp)
            .background(Color(0xFFF5F5F5))
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 16.dp)
        ) {
            items(services) { service ->
                var isExpanded by remember { mutableStateOf(false) }
                val uriHandler = LocalUriHandler.current

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = service.iconRes),
                                contentDescription = service.title,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 8.dp),
                                tint = Color(0xFFF58D4D)
                            )
                            Text(
                                text = service.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.right_arrow),
                            contentDescription = "Expand",
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isExpanded) 90f else 0f), // Поворот на 90 градусов
                            tint = Color(0xFFF58D4D)
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = androidx.compose.animation.expandVertically(animationSpec = tween(durationMillis = 500)),
                        exit = androidx.compose.animation.shrinkVertically(animationSpec = tween(durationMillis = 500))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (service.images != null) {
                                service.images.forEach { imageRes ->
                                    Image(
                                        painter = painterResource(id = imageRes),
                                        contentDescription = service.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .padding(bottom = 8.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit

                                    )
                                }
                            }
                            if (service.description != null) {
                                if (service.title == "Правила в номере") {
                                    val annotatedString = buildAnnotatedString {
                                        append("Правила проживания и пользования гостиничными услугами в отеле Riviera Sunrise Resort & SPA\n\n")
                                        withStyle(
                                            style = SpanStyle(
                                                color = Color(0xFF1E90FF),
                                                textDecoration = TextDecoration.Underline
                                            )
                                        ) {
                                            pushStringAnnotation(tag = "URL", annotation = "https://rivierasunrise.ru/rules")
                                            append("Ознакомиться с правилами")
                                        }
                                    }
                                    androidx.compose.foundation.text.ClickableText(
                                        text = annotatedString,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 14.sp,
                                            color = Color(0xFF666666)
                                        ),
                                        onClick = { offset ->
                                            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                                .firstOrNull()?.let { annotation ->
                                                    uriHandler.openUri(annotation.item)
                                                }
                                        },
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                } else {
                                    Text(
                                        text = service.description,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color(0xFFF58D4D)))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}