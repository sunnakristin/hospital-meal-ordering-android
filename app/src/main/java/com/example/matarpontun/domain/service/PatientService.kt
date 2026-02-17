package com.example.matarpontun.domain.service

import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository

class PatientService (
    private val patientRepository: PatientRepository
) {
    suspend fun getPatientsByWard(wardId: Long): Result<List<Patient>> {

        if (wardId <= 0) {
            return Result.failure(IllegalArgumentException("Invalid ward id"))
        }

        return try {
            val patients = patientRepository.getPatientsByWard(wardId)
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}