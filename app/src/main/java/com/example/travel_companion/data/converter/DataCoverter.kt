package com.example.travel_companion.data.converter

import androidx.room.TypeConverter
import com.example.travel_companion.data.entity.TripCategory
import com.example.travel_companion.data.entity.TripType
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTripType(value: TripType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTripType(value: String?): TripType? {
        return value?.let {
            try {
                TripType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun fromTripCategory(value: TripCategory?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTripCategory(value: String?): TripCategory? {
        return value?.let {
            try {
                TripCategory.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}