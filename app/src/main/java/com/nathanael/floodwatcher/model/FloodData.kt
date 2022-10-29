package com.nathanael.floodwatcher.model

import com.google.firebase.Timestamp

data class FloodData(
    val floodLevel: Double = 0.0,
    val precipitation: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now()
)