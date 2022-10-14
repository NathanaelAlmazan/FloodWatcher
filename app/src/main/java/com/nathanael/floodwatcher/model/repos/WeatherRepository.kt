package com.nathanael.floodwatcher.model.repos

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.nathanael.floodwatcher.model.DbCollections
import com.nathanael.floodwatcher.model.FloodData
import com.nathanael.floodwatcher.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
 This page handles all the necessary request
 for the weather screen. The listener updates the
 UI if there is changes in the database.
*/

class WeatherRepository(private val database: FirebaseFirestore) {
    private val weatherApi = WeatherRetrofitHelper.getInstance().create(WeatherApi::class.java)
    private var listener: ListenerRegistration? = null

    fun addDbListener(
        onDocumentEvent: (FloodData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val query = database.collection(DbCollections.WEATHER.db)
            .document("floodData")

        listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val floodData = snapshot.toObject<FloodData>()
                    onDocumentEvent(floodData!!)
                }
            }

    }

    fun removeDbListener() {
        listener?.remove()
    }

    suspend fun fetchWeatherData(latitude: Double, longitude: Double): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                val result = weatherApi
                    .getWeatherData(latitude, longitude)
                    .body() ?: throw Exception("Weather is null.")

                val weatherData = WeatherData(
                    result.weather[0].main,
                    result.weather[0].description,
                    result.main.temp,
                    result.main.pressure,
                    result.main.humidity,
                    result.visibility,
                    result.wind.speed,
                    result.wind.deg
                )
                Result.Success(weatherData)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}