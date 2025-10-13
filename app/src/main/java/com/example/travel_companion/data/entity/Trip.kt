package com.example.travel_companion.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.travel_companion.data.converter.DateConverter
import java.util.Date

@Entity(tableName = "trips")
@TypeConverters(DateConverter::class)
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val destination: String,
    val startDate: Date,
    val endDate: Date?,
    val tripType: TripType,
    val isActive: Boolean = false,
    val totalDistance: Double = 0.0, // in kilometers
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),

    // Optional fields
    val description: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null,

    // Extended fields for better trip classification
    val category: TripCategory? = null,
    val budget: Double? = null,
    val rating: Int? = null // 1-5 stars
)

enum class TripType {
    LOCAL,          // Viaggio locale (entro città)
    DAY_TRIP,       // Gita giornaliera (fuori città, ritorno stesso giorno)
    MULTI_DAY,      // Viaggio multi-giorno (vacanza, viaggio lungo)
    WEEKEND,        // Viaggio weekend (2-3 giorni)
    BUSINESS,       // Viaggio di lavoro
    ADVENTURE;      // Avventura/Escursione

    fun getDisplayName(): String {
        return when (this) {
            LOCAL -> "Local Trip"
            DAY_TRIP -> "Day Trip"
            MULTI_DAY -> "Multi-Day Trip"
            WEEKEND -> "Weekend Getaway"
            BUSINESS -> "Business Trip"
            ADVENTURE -> "Adventure"
        }
    }

    fun getDescription(): String {
        return when (this) {
            LOCAL -> "Short trip within your city or local area"
            DAY_TRIP -> "Day excursion, returning the same day"
            MULTI_DAY -> "Extended trip lasting multiple days"
            WEEKEND -> "Weekend trip (2-3 days)"
            BUSINESS -> "Work-related travel"
            ADVENTURE -> "Outdoor adventure or exploration"
        }
    }

    fun getIcon(): String {
        return when (this) {
            LOCAL -> "🏙️"
            DAY_TRIP -> "🚗"
            MULTI_DAY -> "✈️"
            WEEKEND -> "🎒"
            BUSINESS -> "💼"
            ADVENTURE -> "🏔️"
        }
    }

    fun getRecommendedDuration(): Pair<Int, Int> {
        // Returns min and max days
        return when (this) {
            LOCAL -> Pair(0, 1)
            DAY_TRIP -> Pair(1, 1)
            MULTI_DAY -> Pair(3, 30)
            WEEKEND -> Pair(2, 3)
            BUSINESS -> Pair(1, 7)
            ADVENTURE -> Pair(1, 14)
        }
    }

    fun requiresMultiDayTracking(): Boolean {
        return this in listOf(MULTI_DAY, WEEKEND, BUSINESS, ADVENTURE)
    }
}

// Nuova enum per categorizzare i viaggi
enum class TripCategory {
    LEISURE,        // Vacanza/Svago
    WORK,           // Lavoro
    FAMILY,         // Famiglia
    FRIENDS,        // Amici
    SOLO,           // Da solo
    ROMANTIC,       // Romantico
    CULTURAL,       // Culturale
    NATURE,         // Natura
    SPORTS;         // Sport

    fun getDisplayName(): String {
        return when (this) {
            LEISURE -> "Leisure"
            WORK -> "Work"
            FAMILY -> "Family"
            FRIENDS -> "Friends"
            SOLO -> "Solo Travel"
            ROMANTIC -> "Romantic"
            CULTURAL -> "Cultural"
            NATURE -> "Nature"
            SPORTS -> "Sports"
        }
    }

    fun getIcon(): String {
        return when (this) {
            LEISURE -> "🌴"
            WORK -> "💼"
            FAMILY -> "👨‍👩‍👧‍👦"
            FRIENDS -> "👥"
            SOLO -> "🚶"
            ROMANTIC -> "💑"
            CULTURAL -> "🏛️"
            NATURE -> "🌿"
            SPORTS -> "⚽"
        }
    }
}

// Entità per i waypoint/coordinate del viaggio
@Entity(tableName = "trip_locations")
@TypeConverters(DateConverter::class)
data class TripLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tripId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestamp: Date = Date(),
    val accuracy: Float? = null,
    val speed: Float? = null
)

// Entità per le foto del viaggio
@Entity(tableName = "trip_photos")
@TypeConverters(DateConverter::class)
data class TripPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tripId: Long,
    val photoUri: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Date = Date(),
    val caption: String? = null
)

// Entità per le note del viaggio
@Entity(tableName = "trip_notes")
@TypeConverters(DateConverter::class)
data class TripNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tripId: Long,
    val content: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Date = Date()
)

// Data class per statistiche per tipo di viaggio
data class TripTypeStatistics(
    val tripType: TripType,
    val count: Int,
    val totalDistance: Double,
    val averageDistance: Double,
    val lastTripDate: Date?
)