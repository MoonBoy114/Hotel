package com.example.hotel.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hotel.R

// Модель данных для отелей
data class Hotel(
    val name: String,
    val location: String,
    val description: String,
    val imageRes: Int,
    val website: String
)

@Composable
fun HotelScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    // Список отелей
    val hotels = listOf(
        Hotel(
            name = "Riviera Sunrise Resort & Spa 5*",
            location = "АЛУШТА, КРЫМ",
            description = """
                Приглашаем в отель Riviera Sunrise Resort & Spa 5*, расположенный в самом центре набережной Алушты и представляющий собой гостиничный комплекс, состоящий из двух зданий.
                
                Современное здание Modern, открытое в 2013 году, и памятник архитектуры - историческое здание Classic. Комфортные номера и высокий уровень обслуживания дополняет собственный пляж, парковая территория, комплекс открытых бассейнов, современный конференц-центр, а также стильный Spa-центр The Shore Spa площадью 2000 кв.м.
                
                Элегантный современный 10-ти этажный корпус Modern был построен под управлением всемирно известного гостиничного оператора The Rezidor Hotel Group. Правильное сочетание цветовых оттенков, использованное в дизайне номеров, создает удивительное чувство комфорта и уюта.
                
                Классический 4-х этажный корпус Classic был открыт в 1913 году и полностью отреставрирован в 2016 году. Номера с просторными балконами, мраморная лестница, украшенная коваными перилами, и итальянская мебель передают историю и особую атмосферу Викторианской эпохи конца XIX века.
                
                Уникальное преимущество отеля - собственный живописный парк на берегу Черного моря, уединенное расположение пляжа и соседство с зоной бассейнов. На территории расположены два открытых бассейна и крытый бассейн в spa-центре с джакузи и детской чашей.
                
                Разнообразие баров и ресторанов удовлетворит самого взыскательного гостя. В ресторане «The Grilled» ежедневно накрывается завтрак, обед и ужин европейской кухни в формате «Шведский стол». Ресторан «Tavrika» идеально подходит для закрытого обеда или ужина, а также для проведения праздничного мероприятия.
                
                Все это создает умиротворяющую атмосферу для беззаботного отдыха и наслаждения природой побережья. Отель расположен в первой линии - прямо у моря. Добраться к нему просто, как общественным транспортом, так и на такси, или воспользовавшись трансфером.
            """.trimIndent(),
            imageRes = R.drawable.riviera_hotel,
            website = "https://rivierasunrise.ru/"
        ),
        Hotel(
            name = "Отель «Cipresso 4*»",
            location = "АЛУШТА, КРЫМ",
            description = """
                Укрытый среди кипарисов и гор Отель Cipresso 4* открыл свои двери в 2020 году. Гостей отеля ждут уютные номера из натуральных материалов, роскошный SPA-комплекс с открытыми и крытыми бассейнами и потрясающие виды на горную гряду Алушты, гору Демерджи и Чатыр-Даг.
                
                На выбор гостей 36 номеров в пастельных тонах, мебель и отделка выполнены из натуральных материалов. В каждом номере есть большая терраса с панорамным видом на горы и величественные кипарисы, а с верхних этажей открывается вид на море.
                
                В отеле функционирует роскошный SPA-комплекс с несколькими видами бань, крытым и открытым бассейном, купелью, джакузи и тренажерным залом. В SPA-комплексе можно заказать услуги косметологии и массажа.
                
                Отель удобно расположен вблизи набережной Алушты — всего через 5 минут ходьбы вы окажетесь на галечном пляже и рядом с достопримечательностями города. Завтраки подаются в формате «шведский стол» или по меню, в зависимости от загрузки отеля. Для маленьких гостей работает детская комната и библиотека.
                
                Отель Cipresso 4* — финалист Национальной гостиничной премии 2021 в номинации «Лучший малый отель»; участник премии Russian Hospitality Awards 2021 в номинации «Открытие года».
            """.trimIndent(),
            imageRes = R.drawable.cipresso_hotel,
            website = "https://cipresso-hotel.ru/"
        ),
        Hotel(
            name = "Отель «Galla Palmira 3*»",
            location = "ВИТИЗЕВО, АНАПА",
            description = """
                Отель Gala Palmira расположился в курортном поселке Витязево на берегу Черного моря. Инфраструктура отеля позволяет погрузиться в отдых, не отвлекаясь на повседневные заботы: питание организовано по системе “все включено”, для гостей работают открытый подогреваемый бассейн, бар, шатры-беседки и детская площадка. Увлекательная анимация для взрослых и детей зарядит вас яркими эмоциями.
                
                Современные лаконичные номера оснащены для комфортного проживания гостей. В большинстве номеров есть уютные балконы, на которых приятно проводить вечера с книжкой или встречать закат.
                
                Отель удачно расположен, после короткой прогулки вы окажетесь у песчаного пляжа и ласкового моря, идеального для отдыха с детьми благодаря пологому берегу. Рядом находятся и главные развлечения курорта — пешеходная улица Паралия, парк развлечений Сказка, аквапарк Олимпия и боулинг Витязь.
                
                Проведите беззаботный отдых всей семьей вместе с Gala Palmira!
            """.trimIndent(),
            imageRes = R.drawable.galla_hotel,
            website = "https://galapalmira.ru/"
        ),
        Hotel(
            name = "Отель «Журавли 4*»",
            location = "ВИТИЗЕВО, АНАПА",
            description = """
                Погрузитесь в атмосферу гостеприимного южного курорта с его лазурным морем и золотыми песчаными пляжами.
                
                Отель ”Журавли” — современный семейный отель на солнечном Черноморском побережье в районе Джемете, Анапа. После короткой прогулки вы окажетесь на пляже с мягким песком и живописными барханами, а те, кто предпочитает уединенный отдых, могут расслабиться в одном из бассейнов отеля.
                
                На собственной закрытой территории отеля ”Журавли” есть ресторан, детская комната и площадка, уютные беседки и зоны отдыха, где можно провести приятный вечер в кругу семьи и друзей.
            """.trimIndent(),
            imageRes = R.drawable.juravli_hotel,
            website = "https://zhuravli-hotel.ru/"
        ),
        Hotel(
            name = "Санаторий «Минеральные воды»",
            location = "СТАВРОПОЛЬСКИЙ КРАЙ",
            description = """
                Санаторий «Минеральные Воды» расположен в живописной Новотерской долине, примыкающей к Железноводскому курорту и Бештаугорскому заповеднику, в окружении гор Машук, Бештау, Развалка, Змейка, Железная, Лысая, на берегу живописного озера и в 3 км от Лечебного парка г. Железноводска, с которым санаторий соединяет терренкурная дорожка. Санаторий удобно расположен в 16 км от аэропорта и железнодорожного вокзала г. Минеральные воды - дорога занимает всего 20 минут.
                
                Санаторий «Минеральные Воды» - многопрофильная здравница. Мы лечим заболевания органов пищеварения, обмена веществ, эндокринной, нервной, костно-мышечной системы, опорно-двигательного аппарата, органов дыхания, органов мужской и женской половой сферы.
                
                Гостям, которые не нуждаются в лечении, мы предлагаем целый комплекс программ и процедур, направленных на общее оздоровление, снятие синдрома хронической усталости, стресса и восстановление сил. Санаторий принимает детей любого возраста (в сопровождении взрослых).
                
                В нашем санатории есть собственный бювет с минеральной водой из скважины № 72 Змейкинского месторождения. Лечебно-профилактические свойства воды из скважины № 72 Змейкинского месторождения уникальны: она помогает избежать заболеваний желудка, поджелудочной железы, почек, печени, желче- и мочевыводящих путей; укрепляет костно-мышечную ткань и нервную систему человека, особенно, связанного с вредными условиями труда.
            """.trimIndent(),
            imageRes = R.drawable.mineral_hotel,
            website = "https://minvody.net/"
        )
    )

    val context = LocalContext.current

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
                    text = "Отели",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Start
                )
            }

            items(hotels) { hotel ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clickable { /* Переход к деталям отеля */ }
                ) {
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Image(
                            painter = painterResource(id = hotel.imageRes),
                            contentDescription = hotel.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = hotel.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.padding(3.dp))
                                Text(
                                    text = hotel.location,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                fontSize = 24.sp,
                                color = Color(0xFFF58D4D)
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
                                    text = hotel.description,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                                if (hotel.website.isNotBlank()) {
                                    Text(
                                        text = hotel.website,
                                        fontSize = 14.sp,
                                        color = Color(0xFFF58D4D),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(hotel.website))
                                                context.startActivity(intent)
                                            }
                                            .padding(bottom = 8.dp)
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