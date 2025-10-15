package com.example.travel_companion.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.travel_companion.util.NotificationHelper
import com.example.travel_companion.worker.TripReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Receiver che viene chiamato quando il dispositivo viene riavviato
 * Riavvia i servizi in background necessari
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed - restarting background services")

            // Crea i canali di notifica
            NotificationHelper.createNotificationChannels(context)

            // Riavvia il WorkManager per le notifiche periodiche
            schedulePeriodicReminders(context)

            // Riavvia Activity Recognition se era attivo
            val prefs = context.getSharedPreferences("travel_companion_prefs", Context.MODE_PRIVATE)
            val activityRecognitionEnabled = prefs.getBoolean("activity_recognition_enabled", false)

            if (activityRecognitionEnabled) {
                ActivityRecognitionManager.startActivityRecognition(context)
                Log.d(TAG, "Activity Recognition restarted")
            }

            // Riavvia Geofencing se c'erano geofence attivi
            val geofencingEnabled = prefs.getBoolean("geofencing_enabled", false)

            if (geofencingEnabled) {
                val geofencingManager = GeofencingManager(context)
                geofencingManager.addPopularPlacesGeofences()
                Log.d(TAG, "Geofencing restarted")
            }

            Log.d(TAG, "All background services restarted successfully")
        }
    }

    private fun schedulePeriodicReminders(context: Context) {
        val reminderWork = PeriodicWorkRequestBuilder<TripReminderWorker>(
            24, TimeUnit.HOURS, // Repeat every 24 hours
            15, TimeUnit.MINUTES // Flex interval
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "trip_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )

        Log.d(TAG, "Periodic reminders scheduled")
    }
}