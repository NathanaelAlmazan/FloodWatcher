package com.nathanael.floodwatcher.screens.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nathanael.floodwatcher.model.FloodData
import com.nathanael.floodwatcher.model.repos.Result
import com.nathanael.floodwatcher.model.WeatherData
import com.nathanael.floodwatcher.model.repos.WeatherRepository
import kotlinx.coroutines.launch

const val BRGY_LATITUDE = 14.647512434076672
const val BRGY_LONGITUDE = 120.96360798231696

class WeatherViewModel(private val repository: WeatherRepository): ViewModel() {
    private var _floodData by mutableStateOf(FloodData())
    private var _weatherData by mutableStateOf(WeatherData())
    private var _errorMessage by mutableStateOf("")

    val floodData: FloodData get() = _floodData
    val weatherData: WeatherData get() = _weatherData
    val errorMessage: String get() = _errorMessage

    fun addListener() {
        viewModelScope.launch {
            repository.addDbListener(::onDocumentEvent, ::onError)
        }
    }

    fun removeListener() {
        viewModelScope.launch {
            repository.removeDbListener()
        }
    }

    fun setWeatherData(lat: Double = BRGY_LATITUDE, lng: Double = BRGY_LONGITUDE) {
        viewModelScope.launch {
            when (val result = repository.fetchWeatherData(lat, lng)) {
                is Result.Success<WeatherData> -> _weatherData = result.data
                is Result.Error -> _errorMessage = result.exception.message.toString()
            }
        }
    }

    private fun onDocumentEvent(data: FloodData) {
        _floodData = data
        setWeatherData()
    }

    private fun onError(exception: Exception) {
        _errorMessage = exception.message!!
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = Firebase.firestore
                WeatherViewModel(WeatherRepository(database))
            }
        }
    }
}