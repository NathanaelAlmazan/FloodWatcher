package com.nathanael.floodwatcher

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class MainViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val _requestLocation = MutableLiveData(false)
    private var _currentScreen by mutableStateOf(savedStateHandle["screen"] ?: "Weather")
    private var _userLocation by mutableStateOf(Location(""))

    val currentScreen: String get() = _currentScreen
    val userLocation: Location get() = _userLocation
    val requestLocation: LiveData<Boolean> = _requestLocation

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
                val savedStateHandle = createSavedStateHandle()
                MainViewModel(savedStateHandle = savedStateHandle)
            }
        }
    }
}