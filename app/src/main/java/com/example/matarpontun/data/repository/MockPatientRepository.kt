package com.example.matarpontun.data.repository

import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository
import kotlinx.coroutines.delay

class MockPatientRepository : PatientRepository {
    override suspend fun getPatientsByWard(wardId: Long): List<Patient> {
        delay(400)

        return when (wardId) {
            1L -> listOf(
                Patient(1, "Arnþór Atli", "101"),
                Patient(2, "Katrín Anna", "102"),
                Patient(3, "Silja Björk", "103"),
                Patient(3, "Sunna Kristín", "104")
            )
            else -> emptyList()
        }
    }
}