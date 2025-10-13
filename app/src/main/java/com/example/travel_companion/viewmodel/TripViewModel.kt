package com.example.travel_companion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.travel_companion.data.database.AppDatabase
import com.example.travel_companion.data.entity.*
import com.example.travel_companion.data.repository.TripRepository
import kotlinx.coroutines.launch
import java.util.Date

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TripRepository

    val allTrips: LiveData<List<Trip>>
    val activeTrip: LiveData<Trip?>
    val tripCount: LiveData<Int>
    val totalDistance: LiveData<Double?>
    val totalPhotos: LiveData<Int>

    private val _operationStatus = MutableLiveData<OperationStatus?>()
    val operationStatus: LiveData<OperationStatus?> = _operationStatus

    init {
        val tripDao = AppDatabase.getDatabase(application).tripDao()
        repository = TripRepository(tripDao)

        allTrips = repository.allTrips
        activeTrip = repository.activeTrip
        tripCount = repository.tripCount
        totalDistance = repository.totalDistance
        totalPhotos = repository.totalPhotos
    }

    // Trip CRUD operations
    fun createTrip(
        destination: String,
        startDate: Date,
        endDate: Date?,
        tripType: TripType,
        description: String? = null,
        category: TripCategory? = null,
        budget: Double? = null,
        rating: Int? = null
    ) {
        viewModelScope.launch {
            try {
                val tripId = repository.createTrip(
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    tripType = tripType,
                    description = description,
                    category = category,
                    budget = budget,
                    rating = rating
                )
                _operationStatus.value = OperationStatus.Success("Trip created successfully", tripId)
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to create trip: ${e.message}")
            }
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.updateTrip(trip)
                _operationStatus.value = OperationStatus.Success("Trip updated successfully")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to update trip: ${e.message}")
            }
        }
    }

    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(tripId)
                _operationStatus.value = OperationStatus.Success("Trip deleted successfully")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to delete trip: ${e.message}")
            }
        }
    }

    fun getTripById(tripId: Long): LiveData<Trip?> {
        return repository.getTripByIdLive(tripId)
    }

    // Trip control operations
    fun startTrip(tripId: Long) {
        viewModelScope.launch {
            try {
                repository.startTrip(tripId)
                _operationStatus.value = OperationStatus.Success("Trip started")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to start trip: ${e.message}")
            }
        }
    }

    fun stopTrip(tripId: Long) {
        viewModelScope.launch {
            try {
                repository.stopTrip(tripId)
                _operationStatus.value = OperationStatus.Success("Trip stopped")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to stop trip: ${e.message}")
            }
        }
    }

    // Location operations
    fun addLocation(
        tripId: Long,
        latitude: Double,
        longitude: Double,
        altitude: Double? = null,
        accuracy: Float? = null,
        speed: Float? = null
    ) {
        viewModelScope.launch {
            try {
                repository.addLocation(tripId, latitude, longitude, altitude, accuracy, speed)
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to add location: ${e.message}")
            }
        }
    }

    fun getLocationsByTrip(tripId: Long): LiveData<List<TripLocation>> {
        return repository.getLocationsByTrip(tripId)
    }

    // Photo operations
    fun addPhoto(
        tripId: Long,
        photoUri: String,
        latitude: Double? = null,
        longitude: Double? = null,
        caption: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.addPhoto(tripId, photoUri, latitude, longitude, caption)
                _operationStatus.value = OperationStatus.Success("Photo added")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to add photo: ${e.message}")
            }
        }
    }

    fun deletePhoto(photo: TripPhoto) {
        viewModelScope.launch {
            try {
                repository.deletePhoto(photo)
                _operationStatus.value = OperationStatus.Success("Photo deleted")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to delete photo: ${e.message}")
            }
        }
    }

    fun getPhotosByTrip(tripId: Long): LiveData<List<TripPhoto>> {
        return repository.getPhotosByTrip(tripId)
    }

    // Note operations
    fun addNote(
        tripId: Long,
        content: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            try {
                repository.addNote(tripId, content, latitude, longitude)
                _operationStatus.value = OperationStatus.Success("Note added")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to add note: ${e.message}")
            }
        }
    }

    fun deleteNote(note: TripNote) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
                _operationStatus.value = OperationStatus.Success("Note deleted")
            } catch (e: Exception) {
                _operationStatus.value = OperationStatus.Error("Failed to delete note: ${e.message}")
            }
        }
    }

    fun getNotesByTrip(tripId: Long): LiveData<List<TripNote>> {
        return repository.getNotesByTrip(tripId)
    }

    // Filter operations
    fun getTripsByType(type: TripType): LiveData<List<Trip>> {
        return repository.getTripsByType(type)
    }

    fun getTripsInDateRange(startDate: Date, endDate: Date): LiveData<List<Trip>> {
        return repository.getTripsInDateRange(startDate, endDate)
    }

    // Trip Type Statistics
    fun getTripCountByType(type: TripType): LiveData<Int> {
        return repository.getTripCountByType(type)
    }

    fun getTotalDistanceByType(type: TripType): LiveData<Double?> {
        return repository.getTotalDistanceByType(type)
    }

    fun getAverageDistanceByType(type: TripType): LiveData<Double?> {
        return repository.getAverageDistanceByType(type)
    }

    fun getTripCountByCategory(category: TripCategory): LiveData<Int> {
        return repository.getTripCountByCategory(category)
    }

    fun getAverageRating(): LiveData<Double?> {
        return repository.getAverageRating()
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }
}

sealed class OperationStatus {
    data class Success(val message: String, val data: Any? = null) : OperationStatus()
    data class Error(val message: String) : OperationStatus()
}