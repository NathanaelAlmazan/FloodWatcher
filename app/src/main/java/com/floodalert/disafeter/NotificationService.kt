package com.floodalert.disafeter

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.floodalert.disafeter.MainActivity.Companion.CHANNEL_ID
import com.floodalert.disafeter.R
import com.floodalert.disafeter.model.DbCollections
import com.floodalert.disafeter.model.FloodData


class NotificationService: Service() {
    private var previousLevel: Int = 0

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val extras = intent?.extras
        val temperature = extras?.get("temperature") as Double?
        val description = extras?.get("description") as String?

        startForeground(NOTIFICATION_ID, buildNotification(0,0, temperature, description))

        val db = Firebase.firestore
        val query = db.collection(DbCollections.WEATHER.db)
            .document("floodData")

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                stopSelf()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val floodData = snapshot.toObject<FloodData>()
                if (floodData != null) {
                    if (floodData.floodLevel.toInt() != previousLevel) {
                        previousLevel = floodData.floodLevel.toInt()

                        updateNotification(
                            floodData.precipitation.toInt(),
                            floodData.floodLevel.toInt(),
                            temperature,
                            description
                        )
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun buildNotification(rain: Int, floodLevel: Int, temp: Double? = 0.0, desc: String? = ""): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val descCapitalized = desc?.split(" ")?.joinToString(" ") {
            it.replaceFirstChar { char -> char.uppercaseChar() }
        }

        val precipitation = if (rain > 1) "$rain% Rain." else "No Rain."

        val tempInCelsius = if (temp != null) temp - 273.15 else 0.0
        val tempInString = String.format("%.1f", tempInCelsius)
        val title = if (floodLevel > 0) "Flood level reached $floodLevel FT! $precipitation" else "$tempInString°C $descCapitalized, $precipitation"
        val content = if (floodLevel > 0) "View evacuation centers and emergency hotlines" else "View flood history and announcements"
        val icon = if (floodLevel > 0) R.drawable.ic_home_flood else R.drawable.ic_weather_partly_cloudy

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(rain: Int, floodLevel: Int, temp: Double? = 0.0, desc: String? = "") {
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, buildNotification(rain, floodLevel, temp, desc))
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}