package com.example.travel_companion.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.travel_companion.data.entity.*
import java.util.Date

@Dao
interface TripDao {

    // Trip operations
    @Insert
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): Trip?

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripByIdLive(tripId: Long): LiveData<Trip?>

    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun getAllTrips(): LiveData<List<Trip>>

    @Query("SELECT * FROM trips WHERE isActive = 1 LIMIT 1")
    fun getActiveTrip(): LiveData<Trip?>

    @Query("SELECT * FROM trips WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTripSync(): Trip?

    @Query("SELECT * FROM trips WHERE tripType = :type ORDER BY startDate DESC")
    fun getTripsByType(type: TripType): LiveData<List<Trip>>

    @Query("SELECT * FROM trips WHERE startDate >= :startDate AND startDate <= :endDate ORDER BY startDate DESC")
    fun getTripsInDateRange(startDate: Date, endDate: Date): LiveData<List<Trip>>

    @Query("UPDATE trips SET isActive = 0")
    suspend fun deactivateAllTrips()

    @Query("SELECT COUNT(*) FROM trips")
    fun getTripCount(): LiveData<Int>

    @Query("SELECT SUM(totalDistance) FROM trips")
    fun getTotalDistance(): LiveData<Double?>

    // Trip Location operations
    @Insert
    suspend fun insertLocation(location: TripLocation): Long

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getLocationsByTrip(tripId: Long): LiveData<List<TripLocation>>

    @Query("SELECT * FROM trip_locations WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getLocationsByTripSync(tripId: Long): List<TripLocation>

    @Query("DELETE FROM trip_locations WHERE tripId = :tripId")
    suspend fun deleteLocationsByTrip(tripId: Long)

    // Trip Photo operations
    @Insert
    suspend fun insertPhoto(photo: TripPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: TripPhoto)

    @Query("SELECT * FROM trip_photos WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getPhotosByTrip(tripId: Long): LiveData<List<TripPhoto>>

    @Query("DELETE FROM trip_photos WHERE tripId = :tripId")
    suspend fun deletePhotosByTrip(tripId: Long)

    @Query("SELECT COUNT(*) FROM trip_photos")
    fun getTotalPhotoCount(): LiveData<Int>

    // Trip Note operations
    @Insert
    suspend fun insertNote(note: TripNote): Long

    @Delete
    suspend fun deleteNote(note: TripNote)

    @Query("SELECT * FROM trip_notes WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getNotesByTrip(tripId: Long): LiveData<List<TripNote>>

    @Query("DELETE FROM trip_notes WHERE tripId = :tripId")
    suspend fun deleteNotesByTrip(tripId: Long)

    // Complex queries
    @Transaction
    suspend fun deleteCompleteTrip(tripId: Long) {
        deleteLocationsByTrip(tripId)
        deletePhotosByTrip(tripId)
        deleteNotesByTrip(tripId)
        getTripById(tripId)?.let { deleteTrip(it) }
    }
}