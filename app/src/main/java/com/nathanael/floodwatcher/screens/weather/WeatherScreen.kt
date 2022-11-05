package com.nathanael.floodwatcher.screens.weather

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.nathanael.floodwatcher.MainViewModel
import com.nathanael.floodwatcher.R
import com.nathanael.floodwatcher.model.FloodData
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt


// List of Weather Icons based on the current weather
sealed class WeatherIcons(val value: String, @DrawableRes val icon: Int) {
    object Cloudy: WeatherIcons("cloudy", R.drawable.bg_cloudy)
    object PartlyCloudy: WeatherIcons("partlyCloudy", R.drawable.bg_partly_cloudy)
    object Rainy: WeatherIcons("rainy", R.drawable.bg_rainy)
    object Stormy: WeatherIcons("stormy", R.drawable.bg_stormy)
    object Sunny: WeatherIcons("sunny", R.drawable.bg_sunny)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    mainViewModel: MainViewModel,
    onNavigateToFloodHistory: () -> Unit
) {

    // This will listen to the changes in the database and update the UI
    DisposableEffect(viewModel) {
        viewModel.addListener()
        onDispose { viewModel.removeListener() }
    }

    LaunchedEffect(Unit) {
        mainViewModel.hideActionButton = true
        mainViewModel.hideNavbar = false
    }

    val weatherData = viewModel.weatherData
    val floodData = viewModel.floodData
    val floodHistory = viewModel.floodHistory
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"))
    val dayName = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH)

    val weatherIcon = remember { mutableStateOf<WeatherIcons>(WeatherIcons.Cloudy) }
    val weatherDesc = remember { mutableStateOf("") }
    val floodDesc = remember { mutableStateOf("") }
    val temperature = remember { mutableStateOf("") }
    val windSpeed = remember { mutableStateOf("") }
    val floodLabelColor by animateColorAsState(
        targetValue = when (floodDesc.value) {
            "No Flood" -> Color(0xFFFFFFFF)
            "Yellow Warning" -> Color(0xFFFDFD96)
            "Orange Warning" -> Color(0xFFFAC898)
            "Critical Level" -> Color(0xFFFFDAD6)
            else -> Color(0xFFFFFFFF)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(floodData.floodLevel) {
        floodDesc.value = when(floodData.floodLevel.roundToInt()) {
            0 -> "No Flood"
            1 -> "Yellow Warning"
            2 -> "Orange Warning"
            else -> "Critical Level"
        }
    }

    LaunchedEffect(weatherData.title, floodData.precipitation) {
        if (floodData.precipitation > 20 && floodData.precipitation < 60) weatherIcon.value = WeatherIcons.Rainy
        else if (floodData.precipitation > 60 && floodData.precipitation <= 100) weatherIcon.value = WeatherIcons.Stormy
        else {
            when (weatherData.title) {
                "Clear" -> weatherIcon.value = WeatherIcons.PartlyCloudy
                "Clouds" -> weatherIcon.value = WeatherIcons.Cloudy
                "Rain" -> weatherIcon.value = WeatherIcons.Rainy
                "Thunderstorm" -> weatherIcon.value = WeatherIcons.Stormy
                else -> weatherIcon.value = WeatherIcons.Sunny
            }
        }
    }

    // Convert temperature from Kelvin to Celsius
    LaunchedEffect(weatherData.temperature) {
        temperature.value = String.format("%.1f", (weatherData.temperature - 273.15))
    }

    // Convert wind speed from meter per second to miles per second
    LaunchedEffect(weatherData.windSpeed) {
        windSpeed.value = String.format("%.2f", (weatherData.windSpeed * 2.2369))
    }

    // Change the weather description based on the rain percent
    LaunchedEffect(weatherData.desc, floodData.precipitation) {
        weatherDesc.value = when(floodData.precipitation.roundToInt()) {
            in 20..30 -> "Light Rain"
            in 31..60 -> "Moderate Rain"
            in 61..100 -> "Heavy Rain"
            else -> weatherData.desc.split(" ").joinToString(" ") {
                it.replaceFirstChar { char -> char.uppercaseChar() }
            }
        }
    }

    LaunchedEffect(floodData) {
        viewModel.setWeatherData()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // The date text
        Text(
            text = "$dayName, $currentDate",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 40.dp, bottom = 16.dp),
        )

        // The card that holds the flood level
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(end = 8.dp, bottom = 8.dp),
            shape = RoundedCornerShape(0.dp, 120.dp, 120.dp, 0.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (image, text) = createRefs()

                Image(
                    painter = painterResource(R.drawable.bg_flood),
                    contentDescription = null,
                    modifier = Modifier.constrainAs(image) {
                        height = Dimension.matchParent
                        width = Dimension.matchParent
                    },
                    contentScale = ContentScale.FillBounds
                )

                Column(modifier = Modifier
                    .padding(16.dp)
                    .constrainAs(text) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                    }) {
                    Text(
                        text = "${floodData.floodLevel.roundToInt().toString().padStart(2, '0')} FT",
                        style = MaterialTheme.typography.displayLarge,
                        color = floodLabelColor
                    )
                    Text(
                        text = floodDesc.value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = floodLabelColor
                    )
                }
            }
        }

        // The card that holds the temperature and weather description
        Card(
            shape = RoundedCornerShape(120.dp, 0.dp, 0.dp, 120.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(start = 8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = weatherIcon.value,
                        transitionSpec = {
                            slideInHorizontally(animationSpec = tween(durationMillis = 1500)) with
                                    fadeOut(animationSpec = tween(durationMillis = 500))
                        }
                    ) { targetState ->
                        Image(
                            painter = painterResource(targetState.icon),
                            contentDescription = null,
                            modifier = Modifier
                                .width(150.dp)
                                .height(150.dp)
                                .padding(24.dp)
                                .shadow(
                                    elevation = 40.dp,
                                    shape = CircleShape,
                                    clip = false
                                ),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
                Column(modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, true)
                    .padding(16.dp, 12.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${temperature.value}°C",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = weatherDesc.value,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // The section that holds the other weather data
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {

            // Card for humidity
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cloud_percent),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${weatherData.humidity.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Humidity",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            // Card for precipitation value
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_humidity),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = "${floodData.precipitation}%",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Rain",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            // Card for wind speed
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_weather_windy),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    text = windSpeed.value,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "mph",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            // Card for pressure value
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_white_balance_sunny),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = weatherData.pressure.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = " hPa",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Section for flood history
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Flood History",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onNavigateToFloodHistory) {
                Text(text = "See All")
            }
        }

        LazyRow(modifier = Modifier.fillMaxSize()) {
            items(floodHistory.take(10)) { data ->
                FloodHistoryCard(data)
            }
        }
    }
}

@Composable
fun FloodHistoryCard(floodData: FloodData) {
    val recordDate = floodData.timestamp.toDate()
    val dateFormatter = SimpleDateFormat("MMM. dd", Locale.US)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxHeight()
            .width(100.dp)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_waves),
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "${floodData.floodLevel.toInt()} FT",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = dateFormatter.format(recordDate),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
