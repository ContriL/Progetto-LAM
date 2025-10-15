package com.example.travel_companion.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.travel_companion.data.database.AppDatabase
import com.example.travel_companion.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Worker per inviare notifiche periodiche sui viaggi
 */
class TripReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val tripDao = AppDatabase.getDatabase(applicationContext).tripDao()

            // Controlla se ci sono viaggi attivi
            val activeTrip = tripDao.getActiveTripSync()
            if (activeTrip != null) {
                // Calcola durata viaggio
                val duration = Date().time - activeTrip.startDate.time
                val hours = TimeUnit.MILLISECONDS.toHours(duration)

                if (hours >= 1) {
                    NotificationHelper.showTripReminderNotification(
                        applicationContext,
                        "Trip in Progress",
                        "You've been traveling for $hours hours. Don't forget to track your journey!",
                        activeTrip.id
                    )
                }
                return@withContext Result.success()
            }

            // Controlla viaggi passati (memories)
            val allTrips = tripDao.getAllTrips().value ?: emptyList()

            if (allTrips.isNotEmpty()) {
                // Trova l'ultimo viaggio completato
                val lastCompletedTrip = allTrips
                    .filter { !it.isActive && it.endDate != null }
                    .maxByOrNull { it.endDate!! }

                lastCompletedTrip?.let { trip ->
                    val daysSinceTrip = TimeUnit.MILLISECONDS.toDays(
                        Date().time - trip.endDate!!.time
                    )

                    // Notifica ricordo dopo 7 giorni
                    if (daysSinceTrip == 7L) {
                        NotificationHelper.showTripReminderNotification(
                            applicationContext,
                            "Travel Memory 📸",
                            "It's been a week since your trip to ${trip.destination}. How was it?",
                            trip.id
                        )
                    }

                    // Notifica invito a viaggiare se sono passati 30 giorni
                    if (daysSinceTrip >= 30L) {
                        NotificationHelper.showTripReminderNotification(
                            applicationContext,
                            "Time for a new adventure? ✈️",
                            "It's been a while since your last trip. Ready for a new journey?",
                            null
                        )
                    }
                }
            } else {
                // Nessun viaggio mai fatto - invito a iniziare
                NotificationHelper.showTripReminderNotification(
                    applicationContext,
                    "Start your first adventure! 🗺️",
                    "Travel Companion is ready to track your journeys. Create your first trip!",
                    null
                )
            }

            // Statistiche settimanali
            val calendar = Calendar.getInstance()
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                val weekStart = calendar.apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }.time

                val weekEnd = Date()
                val weekTrips = tripDao.getTripsInDateRange(weekStart, weekEnd).value ?: emptyList()

                if (weekTrips.isNotEmpty()) {
                    val totalDistance = weekTrips.sumOf { it.totalDistance }
                    NotificationHelper.showTripReminderNotification(
                        applicationContext,
                        "📊 Weekly Travel Report",
                        "This week: ${weekTrips.size} trips, ${String.format("%.1f", totalDistance)} km traveled!",
                        null
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}