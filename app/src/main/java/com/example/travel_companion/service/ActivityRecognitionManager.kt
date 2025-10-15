package com.example.travel_companion.service

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.travel_companion.data.database.AppDatabase
import com.example.travel_companion.util.NotificationHelper
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service per Activity Recognition - Rileva automaticamente quando l'utente sta viaggiando
 */
object ActivityRecognitionManager {

    private const val TAG = "ActivityRecognition"
    private const val DETECTION_INTERVAL = 30000L // 30 secondi
    private const val CONFIDENCE_THRESHOLD = 70 // 70% confidence

    private var isMonitoring = false

    /**
     * Verifica se il permesso ACTIVITY_RECOGNITION è concesso
     */
    private fun hasActivityRecognitionPermission(context: Context): Boolean {
        // Il permesso ACTIVITY_RECOGNITION è richiesto solo da Android Q (API 29) in poi
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Per versioni precedenti ad Android Q, non serve il permesso
            true
        }
    }

    /**
     * Avvia il monitoraggio delle attività
     */
    fun startActivityRecognition(context: Context) {
        if (isMonitoring) {
            Log.d(TAG, "Activity recognition already running")
            return
        }

        // Verifica il permesso prima di procedere
        if (!hasActivityRecognitionPermission(context)) {
            Log.e(TAG, "ACTIVITY_RECOGNITION permission not granted")
            return
        }

        try {
            val intent = Intent(context, ActivityRecognitionReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            ActivityRecognition.getClient(context)
                .requestActivityUpdates(DETECTION_INTERVAL, pendingIntent)
                .addOnSuccessListener {
                    isMonitoring = true
                    Log.d(TAG, "Activity recognition started successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to start activity recognition", e)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing permission for activity recognition", e)
        }
    }

    /**
     * Ferma il monitoraggio delle attività
     */
    fun stopActivityRecognition(context: Context) {
        if (!isMonitoring) return

        try {
            val intent = Intent(context, ActivityRecognitionReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            ActivityRecognition.getClient(context)
                .removeActivityUpdates(pendingIntent)
                .addOnSuccessListener {
                    isMonitoring = false
                    Log.d(TAG, "Activity recognition stopped")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop activity recognition", e)
        }
    }
}

/**
 * BroadcastReceiver per ricevere gli aggiornamenti delle attività rilevate
 */
class ActivityRecognitionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "ActivityRecognitionRx"

    companion object {
        private const val MIN_CONFIDENCE = 70

        // Attività che indicano un viaggio
        private val TRAVEL_ACTIVITIES = setOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent) ?: return

            val mostProbableActivity = result.mostProbableActivity
            val confidence = mostProbableActivity.confidence

            Log.d(TAG, "Activity detected: ${getActivityName(mostProbableActivity.type)} ($confidence%)")

            // Notifica solo se confidence è alta
            if (confidence >= MIN_CONFIDENCE) {
                handleActivityDetection(context, mostProbableActivity)
            }
        }
    }

    private fun handleActivityDetection(context: Context, activity: DetectedActivity) {
        val activityType = activity.type
        val confidence = activity.confidence

        scope.launch {
            try {
                val tripDao = AppDatabase.getDatabase(context).tripDao()
                val activeTrip = tripDao.getActiveTripSync()

                // Se l'attività indica un viaggio e non c'è un trip attivo
                if (activityType in TRAVEL_ACTIVITIES && activeTrip == null) {
                    // Suggerisci di iniziare un tracking
                    val activityName = getActivityName(activityType)

                    Log.d(TAG, "Travel activity detected without active trip: $activityName")

                    // Mostra notifica solo per attività significative
                    if (activityType == DetectedActivity.IN_VEHICLE ||
                        activityType == DetectedActivity.ON_BICYCLE) {
                        NotificationHelper.showStartTrackingSuggestion(context)
                    }
                }

                // Log dell'attività per debug
                NotificationHelper.showActivityDetectionNotification(
                    context,
                    getActivityName(activityType),
                    confidence
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error handling activity detection", e)
            }
        }
    }

    private fun getActivityName(activityType: Int): String {
        return when (activityType) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "UNIDENTIFIED"
        }
    }
}