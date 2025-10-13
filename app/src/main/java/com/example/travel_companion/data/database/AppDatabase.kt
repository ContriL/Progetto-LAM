package com.example.travel_companion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travel_companion.data.converter.DateConverter
import com.example.travel_companion.data.dao.TripDao
import com.example.travel_companion.data.entity.*

@Database(
    entities = [
        Trip::class,
        TripLocation::class,
        TripPhoto::class,
        TripNote::class
    ],
    version = 2,  // Incrementata per nuovi campi
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to trips table
                database.execSQL("ALTER TABLE trips ADD COLUMN category TEXT")
                database.execSQL("ALTER TABLE trips ADD COLUMN budget REAL")
                database.execSQL("ALTER TABLE trips ADD COLUMN rating INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_companion_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}