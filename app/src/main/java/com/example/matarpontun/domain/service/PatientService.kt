package com.example.matarpontun.domain.service

import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository

/**
 * Domain service for patient operations.
 * Wraps [PatientRepository] calls in [Result] so the ViewModel receives
 * structured success/failure without handling raw exceptions.
 */
class PatientService(
    private val patientRepository: PatientRepository
) {
    /** Returns all patients for [wardId], or a [Result.failure] if the fetch fails. */
    suspend fun getPatientsByWard(wardId: Long): Result<List<Patient>> {
        return try {
            val patients = patientRepository.getPatientsByWard(wardId)
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
