package com.example.travel_companion.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.travel_companion.MainActivity

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    // Channel IDs
    const val CHANNEL_REMINDERS = "trip_reminders"
    const val CHANNEL_GEOFENCE = "geofence_alerts"
    const val CHANNEL_ACTIVITY = "activity_detection"
    const val CHANNEL_TRACKING = "location_tracking"

    // Notification IDs
    const val NOTIFICATION_REMINDER = 1001
    const val NOTIFICATION_GEOFENCE = 1002
    const val NOTIFICATION_ACTIVITY = 1003
    const val NOTIFICATION_TRACKING = 1004

    /**
     * Verifica se il permesso per le notifiche è concesso
     */
    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Per versioni precedenti ad Android 13, non serve il permesso
            true
        }
    }

    /**
     * Mostra una notifica solo se il permesso è stato concesso
     */
    private fun notifyIfPermitted(context: Context, notificationId: Int, notification: android.app.Notification) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Cannot show notification: POST_NOTIFICATIONS permission not granted")
            return
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when showing notification", e)
        }
    }

    /**
     * Crea tutti i canali di notifica necessari
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel per Reminders
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Trip Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders about upcoming trips and travel memories"
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel per Geofencing
            val geofenceChannel = NotificationChannel(
                CHANNEL_GEOFENCE,
                "Location Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when entering or leaving areas of interest"
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel per Activity Detection
            val activityChannel = NotificationChannel(
                CHANNEL_ACTIVITY,
                "Activity Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications about detected travel activities"
                enableVibration(false)
                setShowBadge(false)
            }

            // Channel per Tracking
            val trackingChannel = NotificationChannel(
                CHANNEL_TRACKING,
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing trip tracking notifications"
                enableVibration(false)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(geofenceChannel)
            notificationManager.createNotificationChannel(activityChannel)
            notificationManager.createNotificationChannel(trackingChannel)
        }
    }

    /**
     * Mostra notifica reminder per viaggi
     */
    fun showTripReminderNotification(
        context: Context,
        title: String,
        message: String,
        tripId: Long? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            tripId?.let { putExtra("TRIP_ID", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notifyIfPermitted(context, NOTIFICATION_REMINDER, notification)
    }

    /**
     * Mostra notifica per geofencing
     */
    fun showGeofenceNotification(
        context: Context,
        title: String,
        message: String,
        isEntering: Boolean
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_GEOFENCE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = if (isEntering) "📍" else "👋"

        val notification = NotificationCompat.Builder(context, CHANNEL_GEOFENCE)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("$emoji $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        notifyIfPermitted(context, NOTIFICATION_GEOFENCE, notification)
    }

    /**
     * Mostra notifica per activity detection
     */
    fun showActivityDetectionNotification(
        context: Context,
        activityType: String,
        confidence: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ACTIVITY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = when (activityType) {
            "WALKING" -> "🚶"
            "RUNNING" -> "🏃"
            "ON_BICYCLE" -> "🚴"
            "IN_VEHICLE" -> "🚗"
            else -> "🎯"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ACTIVITY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$emoji Activity Detected")
            .setContentText("$activityType detected with $confidence% confidence")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notifyIfPermitted(context, NOTIFICATION_ACTIVITY, notification)
    }

    /**
     * Mostra notifica suggerimento per iniziare tracking
     */
    fun showStartTrackingSuggestion(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SUGGESTED_ACTION", "START_TRIP")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ACTIVITY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ACTIVITY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🚀 Ready for a trip?")
            .setContentText("It looks like you're traveling. Start tracking?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_input_add,
                "Start Trip",
                pendingIntent
            )
            .build()

        notifyIfPermitted(context, NOTIFICATION_ACTIVITY, notification)
    }

    /**
     * Cancella una notifica specifica
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        try {
            NotificationManagerCompat.from(context).cancel(notificationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification", e)
        }
    }

    /**
     * Cancella tutte le notifiche
     */
    fun cancelAllNotifications(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancelAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling all notifications", e)
        }
    }
}