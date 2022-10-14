package com.nathanael.floodwatcher

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.nathanael.floodwatcher.screens.AppScaffold
import com.nathanael.floodwatcher.theme.FloodWatcherTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    // Location Tracker Dependencies
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set main app content
        setContent {
            FloodWatcherTheme {
                AppScaffold(mainViewModel)
            }
        }

        // Start Location Tracker
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(120)
            fastestInterval = TimeUnit.SECONDS.toMillis(60)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                if (result.lastLocation != null) mainViewModel.setUserLocation(result.lastLocation!!)
            }
        }

        // Request User Location
        requestUserLocation()

        // Ask user to open GPS if GPS is turned off
        mainViewModel.requestLocation.observe(this) {
            if (it && !isLocationEnabled()) requestToEnableLocation()
        }
    }

    private fun requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            startLocationPermissionRequest()
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val requestMultiplePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.entries.all { it.value }) requestUserLocation()
        else Toast.makeText(this, "Location access is denied.", Toast.LENGTH_SHORT).show()
    }

    private fun startLocationPermissionRequest() {
        requestMultiplePermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Check if GPS is on
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    private fun requestToEnableLocation() {
        val settingRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val task: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this)
            .checkLocationSettings(settingRequest)

        task.addOnSuccessListener {
            requestUserLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    companion object {
        const val REQUEST_CHECK_SETTINGS = 999
    }
}