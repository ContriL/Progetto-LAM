package com.example.travel_companion.data.repository

import androidx.lifecycle.LiveData
import com.example.travel_companion.data.dao.TripDao
import com.example.travel_companion.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class TripRepository(private val tripDao: TripDao) {

    // Trip operations
    val allTrips: LiveData<List<Trip>> = tripDao.getAllTrips()
    val activeTrip: LiveData<Trip?> = tripDao.getActiveTrip()
    val tripCount: LiveData<Int> = tripDao.getTripCount()
    val totalDistance: LiveData<Double?> = tripDao.getTotalDistance()
    val totalPhotos: LiveData<Int> = tripDao.getTotalPhotoCount()

    suspend fun createTrip(
        destination: String,
        startDate: Date,
        endDate: Date?,
        tripType: TripType,
        description: String? = null,
        category: TripCategory? = null,
        budget: Double? = null,
        rating: Int? = null
    ): Long = withContext(Dispatchers.IO) {
        // Deactivate any existing active trip
        tripDao.deactivateAllTrips()

        val trip = Trip(
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            tripType = tripType,
            isActive = true,
            description = description,
            category = category,
            budget = budget,
            rating = rating
        )

        tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) = withContext(Dispatchers.IO) {
        tripDao.updateTrip(trip.copy(updatedAt = Date()))
    }

    suspend fun deleteTrip(tripId: Long) = withContext(Dispatchers.IO) {
        tripDao.deleteCompleteTrip(tripId)
    }

    suspend fun getTripById(tripId: Long): Trip? = withContext(Dispatchers.IO) {
        tripDao.getTripById(tripId)
    }

    fun getTripByIdLive(tripId: Long): LiveData<Trip?> {
        return tripDao.getTripByIdLive(tripId)
    }

    suspend fun getActiveTripSync(): Trip? = withContext(Dispatchers.IO) {
        tripDao.getActiveTripSync()
    }

    suspend fun startTrip(tripId: Long) = withContext(Dispatchers.IO) {
        tripDao.deactivateAllTrips()
        getTripById(tripId)?.let { trip ->
            tripDao.updateTrip(trip.copy(isActive = true, updatedAt = Date()))
        }
    }

    suspend fun stopTrip(tripId: Long) = withContext(Dispatchers.IO) {
        getTripById(tripId)?.let { trip ->
            // Calculate total distance from locations
            val locations = tripDao.getLocationsByTripSync(tripId)
            val distance = calculateTotalDistance(locations)

            tripDao.updateTrip(
                trip.copy(
                    isActive = false,
                    endDate = Date(),
                    totalDistance = distance,
                    updatedAt = Date()
                )
            )
        }
    }

    fun getTripsByType(type: TripType): LiveData<List<Trip>> {
        return tripDao.getTripsByType(type)
    }

    fun getTripsInDateRange(startDate: Date, endDate: Date): LiveData<List<Trip>> {
        return tripDao.getTripsInDateRange(startDate, endDate)
    }

    // Location operations
    suspend fun addLocation(
        tripId: Long,
        latitude: Double,
        longitude: Double,
        altitude: Double? = null,
        accuracy: Float? = null,
        speed: Float? = null
    ) = withContext(Dispatchers.IO) {
        val location = TripLocation(
            tripId = tripId,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            accuracy = accuracy,
            speed = speed
        )
        tripDao.insertLocation(location)
    }

    fun getLocationsByTrip(tripId: Long): LiveData<List<TripLocation>> {
        return tripDao.getLocationsByTrip(tripId)
    }

    // Photo operations
    suspend fun addPhoto(
        tripId: Long,
        photoUri: String,
        latitude: Double? = null,
        longitude: Double? = null,
        caption: String? = null
    ) = withContext(Dispatchers.IO) {
        val photo = TripPhoto(
            tripId = tripId,
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude,
            caption = caption
        )
        tripDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: TripPhoto) = withContext(Dispatchers.IO) {
        tripDao.deletePhoto(photo)
    }

    fun getPhotosByTrip(tripId: Long): LiveData<List<TripPhoto>> {
        return tripDao.getPhotosByTrip(tripId)
    }

    // Note operations
    suspend fun addNote(
        tripId: Long,
        content: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) = withContext(Dispatchers.IO) {
        val note = TripNote(
            tripId = tripId,
            content = content,
            latitude = latitude,
            longitude = longitude
        )
        tripDao.insertNote(note)
    }

    suspend fun deleteNote(note: TripNote) = withContext(Dispatchers.IO) {
        tripDao.deleteNote(note)
    }

    fun getNotesByTrip(tripId: Long): LiveData<List<TripNote>> {
        return tripDao.getNotesByTrip(tripId)
    }

    // Trip Type Statistics
    fun getTripCountByType(type: TripType): LiveData<Int> {
        return tripDao.getTripCountByType(type.name)
    }

    fun getTotalDistanceByType(type: TripType): LiveData<Double?> {
        return tripDao.getTotalDistanceByType(type.name)
    }

    fun getAverageDistanceByType(type: TripType): LiveData<Double?> {
        return tripDao.getAverageDistanceByType(type.name)
    }

    fun getTripCountByCategory(category: TripCategory): LiveData<Int> {
        return tripDao.getTripCountByCategory(category.name)
    }

    fun getAverageRating(): LiveData<Double?> {
        return tripDao.getAverageRating()
    }

    // Utility function to calculate distance
    private fun calculateTotalDistance(locations: List<TripLocation>): Double {
        if (locations.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until locations.size - 1) {
            val loc1 = locations[i]
            val loc2 = locations[i + 1]
            totalDistance += calculateDistance(
                loc1.latitude, loc1.longitude,
                loc2.latitude, loc2.longitude
            )
        }
        return totalDistance
    }

    // Haversine formula for distance calculation
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}