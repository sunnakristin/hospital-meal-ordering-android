package com.example.matarpontun.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.matarpontun.data.local.dao.DailyOrderDao
import com.example.matarpontun.data.local.dao.PatientDao
import com.example.matarpontun.data.local.entity.DailyOrderEntity
import com.example.matarpontun.data.local.entity.PatientEntity

/**
 * Room database for the app. Contains two tables:
 * - [PatientEntity] — cached patient list per ward for offline access
 * - [DailyOrderEntity] — cached daily orders per ward/patient/date
 *
 * Uses [fallbackToDestructiveMigration] so schema changes during development
 * wipe the cache rather than requiring explicit migration scripts.
 */
@Database(entities = [DailyOrderEntity::class, PatientEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailyOrderDao(): DailyOrderDao
    abstract fun patientDao(): PatientDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /** Returns the singleton database instance, creating it if necessary. Thread-safe. */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "matarpontun.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
