package com.nathanael.floodwatcher

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nathanael.floodwatcher.model.WeatherData
import com.nathanael.floodwatcher.model.repos.Result
import com.nathanael.floodwatcher.model.repos.WeatherRepository
import com.nathanael.floodwatcher.screens.weather.WeatherViewModel
import kotlinx.coroutines.launch

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val weatherRepository: WeatherRepository
): ViewModel() {
    private val _requestLocation = MutableLiveData(false)
    private val _weatherData: MutableLiveData<WeatherData> by lazy {
        MutableLiveData<WeatherData>().also {
            viewModelScope.launch {
                val lat: Double = WeatherViewModel.BRGY_LATITUDE
                val lng: Double = WeatherViewModel.BRGY_LONGITUDE

                when (val result = weatherRepository.fetchWeatherData(lat, lng)) {
                    is Result.Success<WeatherData> -> it.postValue(result.data)
                    is Result.Error -> _errorMessage = result.exception.message.toString()
                }
            }
        }
    }

    private var _currentScreen by mutableStateOf(savedStateHandle["screen"] ?: "Weather")
    private var _userLocation by mutableStateOf(Location(""))
    private var _errorMessage by mutableStateOf<String?>(null)

    val userLocation: Location get() = _userLocation
    val requestLocation: LiveData<Boolean> = _requestLocation
    val weatherData: LiveData<WeatherData> = _weatherData

    fun setScreen(screen: String) {
        savedStateHandle["screen"] = screen
        _currentScreen = screen
    }

    fun requestUserLocation() {
        _requestLocation.value = false
        _requestLocation.value = true
    }

    fun setUserLocation(location: Location) {
        _userLocation = location
    }

    // Initialize view model
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = Firebase.firestore
                val savedStateHandle = createSavedStateHandle()
                val weatherRepository = WeatherRepository(db)
                MainViewModel(savedStateHandle, weatherRepository)
            }
        }
    }
}