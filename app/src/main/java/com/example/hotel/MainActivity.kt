package com.example.hotel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hotel.data.repository.HotelRepository
import com.example.hotel.data.viewmodel.HotelViewModel
import com.example.hotel.data.viewmodel.HotelViewModelFactory
import com.example.hotel.detailscreens.NewsDetailScreen
import com.example.hotel.detailscreens.ServiceDetailScreen
import com.example.hotel.makers.MakerForNews
import com.example.hotel.makers.MakerForRooms
import com.example.hotel.makers.MakerForService
import com.example.hotel.screens.AboutMe
import com.example.hotel.screens.AnimationScreen
import com.example.hotel.screens.DevicesScreen
import com.example.hotel.screens.FillUpScreen
import com.example.hotel.screens.HomeScreen
import com.example.hotel.screens.HotelScreen
import com.example.hotel.screens.LoginScreen
import com.example.hotel.screens.NewsScreen
import com.example.hotel.screens.ProfileScreen
import com.example.hotel.screens.RegisterScreen
import com.example.hotel.screens.RestaurantScreen
import com.example.hotel.screens.RoomScreen
import com.example.hotel.screens.SPAScreen
import com.example.hotel.screens.ServicesScreen
import com.example.hotel.screens.TransferScreen

import com.example.hotel.ui.theme.HotelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HotelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(HotelRepository()))
                    AppNavigation(navController, viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: HotelViewModel) {
    val currentUser by viewModel.currentUser.observeAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) "login" else "home"
    ) {
        composable("login") {
            LoginScreen(viewModel, navController)
        }
        composable("register") {
            RegisterScreen(viewModel, navController)
        }
        composable("news") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()

            ) { paddingValues ->
                NewsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues),
                    navController = navController
                )
            }
        }
        composable("home") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                
            ) { paddingValues ->
                HomeScreen(viewModel, navController,Modifier.padding(paddingValues))
            }
        }
        composable("profile") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()

            ) { paddingValues ->
                ProfileScreen(viewModel, navController, Modifier.padding(paddingValues))
            }
        }
        composable("makerForNews") {
            MakerForNews(
                viewModel = viewModel,
                navController = navController,
                newsId = null
            )
        }
        composable(
            route = "makerForNews/{newsId}",
            arguments = listOf(navArgument("newsId") { nullable = true })
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId")
            MakerForNews(
                viewModel = viewModel,
                navController = navController,
                newsId = newsId
            )
        }
        composable("newsDetail/{newsId}") { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
            NewsDetailScreen(
                viewModel = viewModel,
                newsId = newsId,
                navController = navController
            )
        }
        composable("rooms") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) { paddingValues ->
                RoomScreen(
                    viewModel = viewModel,
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
        composable("makerForRooms") {
            MakerForRooms(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = "makerForRooms/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId")
            MakerForRooms(
                viewModel = viewModel,
                navController = navController,
                roomId = roomId
            )
        }
        composable("aboutMe"
        ) {
            AboutMe(viewModel = viewModel, navController = navController)
        }
        composable("makerForService/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId")
            MakerForService(viewModel = viewModel, navController = navController, serviceId = serviceId)
        }

        composable("makerForService") {
            MakerForService(viewModel = viewModel, navController = navController)
        }
        composable("serviceDetail/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            ServiceDetailScreen(
                viewModel = viewModel,
                serviceId = serviceId,
                navController = navController
            )
        }
        composable("devices") {
            DevicesScreen(navController = navController)
        }
        composable("services") {
            ServicesScreen(navController = navController)
        }
        composable("fillUp") {
            FillUpScreen(navController = navController)
        }
        composable("transfer") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) { paddingValues ->
                TransferScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            }
        }
        composable("animation") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) { paddingValues ->
                AnimationScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            }
        }
        composable("restaurant") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) { paddingValues ->
                RestaurantScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            }
        }
        composable("hotel") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) { paddingValues ->
                HotelScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            }
        }
        composable("spa") {
            Scaffold(
                bottomBar = { OrangeNavigationBar(navController) },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) { paddingValues ->
                SPAScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrangeNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFF58D4D),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFFF58D4D))
            .imeNestedScroll()
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_news),
                    contentDescription = "Новости",
                    modifier = Modifier.size(18.dp), // Уменьшаем размер иконки
                    tint = if (currentRoute == "news") Color.White else Color.White.copy(alpha = 0.7f)
                )
            },
            label = {
                Text(
                    text = "Новости",
                    fontSize = 10.sp // Уменьшаем размер текста
                )
            },
            selected = currentRoute == "news",
            onClick = {
                navController.navigate("news") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color.White.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Главная",
                    modifier = Modifier.size(18.dp), // Уменьшаем размер иконки
                    tint = if (currentRoute == "home") Color.White else Color.White.copy(alpha = 0.7f)
                )
            },
            label = {
                Text(
                    text = "Главная",
                    fontSize = 10.sp // Уменьшаем размер текста
                )
            },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color.White.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Профиль",
                    modifier = Modifier.size(18.dp),
                    tint = if (currentRoute == "profile") Color.White else Color.White.copy(alpha = 0.7f)
                )
            },
            label = {
                Text(
                    text = "Профиль",
                    fontSize = 10.sp
                )
            },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                selectedTextColor = Color.White,
                unselectedTextColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}