package com.example.matarpontun.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.matarpontun.data.local.entity.PatientEntity

/**
 * Room DAO for [PatientEntity].
 * REPLACE conflict strategy means a fresh fetch always overwrites stale cached rows.
 */
@Dao
interface PatientDao {

    /** Inserts or replaces all patients for a ward. Used after a successful network fetch. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<PatientEntity>)

    /** Returns all cached patients for [wardId]. */
    @Query("SELECT * FROM patients WHERE wardId = :wardId")
    suspend fun getPatientsByWard(wardId: Long): List<PatientEntity>
}
