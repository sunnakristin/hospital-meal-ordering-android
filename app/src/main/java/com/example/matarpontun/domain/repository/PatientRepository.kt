package com.example.matarpontun.domain.repository

import com.example.matarpontun.domain.model.Patient

interface PatientRepository {
    suspend fun getPatientsByWard(wardId: Long): List<Patient>
}