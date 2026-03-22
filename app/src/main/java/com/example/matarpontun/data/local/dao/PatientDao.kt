package com.example.matarpontun.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.matarpontun.data.local.entity.PatientEntity

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<PatientEntity>)

    @Query("SELECT * FROM patients WHERE wardId = :wardId")
    suspend fun getPatientsByWard(wardId: Long): List<PatientEntity>
}
