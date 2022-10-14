package com.nathanael.floodwatcher.model

// The tables in the database
enum class DbCollections(val db: String) {
    WEATHER("weather"),
    EVACUATE("evacuation")
}