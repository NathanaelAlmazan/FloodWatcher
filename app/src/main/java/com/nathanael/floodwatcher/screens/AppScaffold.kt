package com.nathanael.floodwatcher.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nathanael.floodwatcher.MainViewModel
import com.nathanael.floodwatcher.R
import com.nathanael.floodwatcher.screens.emergency.EmergencyScreen
import com.nathanael.floodwatcher.screens.evacuate.EvacuateMapScreen
import com.nathanael.floodwatcher.screens.evacuate.EvacuateScreen
import com.nathanael.floodwatcher.screens.evacuate.EvacuateViewModel
import com.nathanael.floodwatcher.screens.weather.FloodHistoryScreen
import com.nathanael.floodwatcher.screens.weather.WeatherScreen
import com.nathanael.floodwatcher.screens.weather.WeatherViewModel

sealed class Screen(val route: String, val title: String, @DrawableRes val icon: Int) {
    object Weather : Screen("weatherScreen", "Weather", R.drawable.ic_weather_partly_cloudy)
    object FloodHistory : Screen("floodHistory", "History", R.drawable.ic_weather_partly_cloudy)
    object Evacuate : Screen("evacuateScreen", "Evacuate", R.drawable.ic_run_fast)
    object EvacuateMap: Screen("evacuateMap", "EvacuateMap", R.drawable.ic_run_fast)
    object Emergency: Screen("emergencyScreen", "Emergency", R.drawable.ic_ambulance)
    object Bulletin: Screen("bulletinScreen", "Bulletin", R.drawable.ic_view_dashboard)
    object Profile: Screen("profileScreen", "Profile", R.drawable.ic_baseline_account_circle_24)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val darkMode = isSystemInDarkTheme()
    val navigationList = listOf(
        Screen.Weather,
        Screen.Evacuate,
        Screen.Emergency,
        Screen.Bulletin,
        Screen.Profile
    )

    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "Missing view model owner."
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            Color.Transparent,
            darkIcons = darkMode)
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.systemBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        ),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navigationList.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(screen.icon), contentDescription = null) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                            mainViewModel.setScreen(screen.title)
                        }
                    )
                }
            }
        }
    ) {
        AppNavHost(modifier = Modifier.padding(it), navController, mainViewModel, viewModelStoreOwner)
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    viewModelStoreOwner: ViewModelStoreOwner,
    startDestination: String = Screen.Emergency.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Weather.route) {
            val weatherViewModel: WeatherViewModel = viewModel(
                factory = WeatherViewModel.Factory,
                viewModelStoreOwner = viewModelStoreOwner
            )

            WeatherScreen(
                viewModel = weatherViewModel,
                onNavigateToFloodHistory = { navController.navigate(Screen.FloodHistory.route) }
            )
        }
        composable(Screen.FloodHistory.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                FloodHistoryScreen(
                    viewModel = viewModel(),
                    onNavigateToWeather = { navController.navigate(Screen.Weather.route) }
                )
            }
        }
        composable(Screen.Evacuate.route) {
            val evacuateViewModel: EvacuateViewModel = viewModel(
                factory = EvacuateViewModel.Factory,
                viewModelStoreOwner = viewModelStoreOwner
            )

            EvacuateScreen(
                viewModel = evacuateViewModel,
                mainViewModel = mainViewModel,
                onNavigateToMap = { navController.navigate(Screen.EvacuateMap.route) }
            )
        }
        composable(Screen.EvacuateMap.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                EvacuateMapScreen(
                    mainViewModel = mainViewModel,
                    viewModel = viewModel(),
                    onNavigateToMain = { navController.navigate(Screen.Evacuate.route) }
                )
            }
        }
        composable(Screen.Emergency.route) {
            EmergencyScreen()
        }
    }
}