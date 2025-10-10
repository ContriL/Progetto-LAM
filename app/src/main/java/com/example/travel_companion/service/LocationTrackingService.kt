package com.example.travel_companion.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.travel_companion.MainActivity
import com.example.travel_companion.data.database.AppDatabase
import com.example.travel_companion.data.repository.TripRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var repository: TripRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentTripId: Long = -1
    private var isTracking = false
    private var totalDistance = 0.0
    private var lastLocation: Location? = null

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val NOTIFICATION_ID = 12345
        private const val CHANNEL_ID = "location_tracking_channel"
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"

        // Location update settings
        private const val UPDATE_INTERVAL = 5000L // 5 seconds
        private const val FASTEST_INTERVAL = 2000L // 2 seconds
        private const val MIN_DISTANCE = 10f // 10 meters
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val tripDao = AppDatabase.getDatabase(applicationContext).tripDao()
        repository = TripRepository(tripDao)

        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_TRACKING -> {
                    currentTripId = it.getLongExtra(EXTRA_TRIP_ID, -1)
                    if (currentTripId != -1L) {
                        startTracking()
                    }
                }
                ACTION_STOP_TRACKING -> {
                    stopTracking()
                }
            }
        }
        return START_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }
    }

    private fun startTracking() {
        if (isTracking) {
            Log.d(TAG, "Already tracking")
            return
        }

        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                UPDATE_INTERVAL
            ).apply {
                setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                setMinUpdateDistanceMeters(MIN_DISTANCE)
            }.build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            isTracking = true
            startForeground(NOTIFICATION_ID, createNotification("Tracking your journey..."))
            Log.d(TAG, "Tracking started for trip: $currentTripId")

        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
            stopSelf()
        }
    }

    private fun stopTracking() {
        if (!isTracking) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking = false

        Log.d(TAG, "Tracking stopped. Total distance: $totalDistance km")

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleLocationUpdate(location: Location) {
        serviceScope.launch {
            try {
                // Calculate distance from last location
                lastLocation?.let { last ->
                    val distance = last.distanceTo(location) / 1000.0 // Convert to km
                    if (distance > 0.001) { // Filter out tiny movements
                        totalDistance += distance
                    }
                }
                lastLocation = location

                // Save to database
                repository.addLocation(
                    tripId = currentTripId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null,
                    accuracy = if (location.hasAccuracy()) location.accuracy else null,
                    speed = if (location.hasSpeed()) location.speed else null
                )

                // Update notification
                updateNotification(location)

                Log.d(TAG, "Location saved: ${location.latitude}, ${location.longitude}")

            } catch (e: Exception) {
                Log.e(TAG, "Error handling location update", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows trip tracking status"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Trip in Progress")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(location: Location) {
        val content = String.format(
            "Distance: %.2f km | Speed: %.1f km/h",
            totalDistance,
            if (location.hasSpeed()) location.speed * 3.6 else 0.0
        )

        val notification = createNotification(content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        Log.d(TAG, "Service destroyed")
    }
}