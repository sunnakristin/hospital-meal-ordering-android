package com.example.matarpontun.domain.repository

import com.example.matarpontun.domain.model.Patient

/**
 * Domain interface for patient data access.
 * Implemented by [OfflineFirstPatientRepository] (with Room fallback)
 * and [NetworkPatientRepository] (network only).
 */
interface PatientRepository {
    /** Returns all patients for the given ward, from network or local cache. */
    suspend fun getPatientsByWard(wardId: Long): List<Patient>
}
