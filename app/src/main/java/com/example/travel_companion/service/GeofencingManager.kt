package com.example.travel_companion.service

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.travel_companion.util.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/**
 * Manager per gestire i Geofence (aree di interesse)
 */
class GeofencingManager(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geofenceList = mutableListOf<Geofence>()

    companion object {
        private const val TAG = "GeofencingManager"
        private const val GEOFENCE_RADIUS_METERS = 500f // 500 metri
        private const val GEOFENCE_EXPIRATION_MILLISECONDS = Geofence.NEVER_EXPIRE
    }

    /**
     * Aggiunge un geofence per una location specifica
     */
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float = GEOFENCE_RADIUS_METERS,
        title: String
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(GEOFENCE_EXPIRATION_MILLISECONDS)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()

        geofenceList.add(geofence)
        registerGeofences()

        Log.d(TAG, "Geofence added: $title at ($latitude, $longitude)")
    }

    /**
     * Aggiunge geofence per luoghi popolari predefiniti
     */
    fun addPopularPlacesGeofences() {
        // Esempio: Aggiunge geofence per luoghi famosi
        val places = listOf(
            GeofencePlace("colosseum", 41.8902, 12.4922, "Colosseum, Rome"),
            GeofencePlace("eiffel_tower", 48.8584, 2.2945, "Eiffel Tower, Paris"),
            GeofencePlace("sagrada", 41.4036, 2.1744, "Sagrada Familia, Barcelona"),
            GeofencePlace("big_ben", 51.5007, -0.1246, "Big Ben, London")
        )

        places.forEach { place ->
            addGeofence(place.id, place.latitude, place.longitude, title = place.name)
        }
    }

    /**
     * Registra tutti i geofence con Google Play Services
     */
    private fun registerGeofences() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Missing location permission")
            return
        }

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()

        val pendingIntent = getGeofencePendingIntent()

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofences registered successfully (${geofenceList.size} geofences)")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to register geofences", e)
            }
    }

    /**
     * Rimuove tutti i geofence
     */
    fun removeAllGeofences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
            .addOnSuccessListener {
                geofenceList.clear()
                Log.d(TAG, "All geofences removed")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofences", e)
            }
    }

    /**
     * Rimuove un geofence specifico
     */
    fun removeGeofence(id: String) {
        geofencingClient.removeGeofences(listOf(id))
            .addOnSuccessListener {
                geofenceList.removeIf { it.requestId == id }
                Log.d(TAG, "Geofence removed: $id")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofence: $id", e)
            }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    data class GeofencePlace(
        val id: String,
        val latitude: Double,
        val longitude: Double,
        val name: String
    )
}

/**
 * BroadcastReceiver per gestire le transizioni dei geofence
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        // Controlla se è un evento ENTER o EXIT
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

            triggeringGeofences.forEach { geofence ->
                handleGeofenceTransition(context, geofence, geofenceTransition)
            }
        } else {
            Log.e(TAG, "Unknown geofence transition: $geofenceTransition")
        }
    }

    private fun handleGeofenceTransition(
        context: Context,
        geofence: Geofence,
        transitionType: Int
    ) {
        val geofenceId = geofence.requestId
        val isEntering = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER

        Log.d(TAG, "Geofence transition: ${if (isEntering) "ENTER" else "EXIT"} - $geofenceId")

        // Determina il nome del luogo dal geofence ID
        val placeName = getPlaceName(geofenceId)

        // Mostra notifica appropriata
        if (isEntering) {
            NotificationHelper.showGeofenceNotification(
                context,
                "Welcome to $placeName!",
                "You've entered an area of interest. Enjoy your visit!",
                isEntering = true
            )
        } else {
            NotificationHelper.showGeofenceNotification(
                context,
                "Leaving $placeName",
                "Hope you enjoyed your time here!",
                isEntering = false
            )
        }

        // TODO: Puoi salvare queste informazioni nel database se vuoi tracciare le visite
    }

    private fun getPlaceName(geofenceId: String): String {
        return when (geofenceId) {
            "colosseum" -> "Colosseum, Rome"
            "eiffel_tower" -> "Eiffel Tower, Paris"
            "sagrada" -> "Sagrada Familia, Barcelona"
            "big_ben" -> "Big Ben, London"
            else -> "Point of Interest"
        }
    }
}

/**
 * Service placeholder per gestire geofencing in background
 */
class GeofencingService : android.app.Service() {

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        Log.d("GeofencingService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("GeofencingService", "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GeofencingService", "Service destroyed")
    }
}