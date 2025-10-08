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
    val notes: String? = null
)

enum class TripType {
    LOCAL,          // Viaggio locale
    DAY_TRIP,       // Gita giornaliera
    MULTI_DAY;      // Viaggio multi-giorno

    fun getDisplayName(): String {
        return when (this) {
            LOCAL -> "Local Trip"
            DAY_TRIP -> "Day Trip"
            MULTI_DAY -> "Multi-Day Trip"
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