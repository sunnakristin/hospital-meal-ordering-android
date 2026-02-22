package com.example.matarpontun.data.repository

import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository
import kotlinx.coroutines.delay

class MockPatientRepository : PatientRepository {
    override suspend fun getPatientsByWard(wardId: Long): List<Patient> {
        delay(400)

        // Define food types once
        val normalFood = FoodType(
            id = 1,
            typeName = "A1",
            description = "Normal food"
        )

        val vegetarianFood = FoodType(
            id = 2,
            typeName = "A2",
            description = "Vegetarian"
        )

        return when (wardId) {
            1L -> listOf(
                Patient(
                    patientId = 1,
                    name = "Arnþór Atli",
                    bedNumber = 1,
                    room = "101",
                    foodType = normalFood,
                    restrictions = emptyList()
                ),
                Patient(
                    patientId = 2,
                    name = "Katrín Anna",
                    bedNumber = 2,
                    room = "102",
                    foodType = vegetarianFood,
                    restrictions = emptyList()
                ),
                Patient(
                    patientId = 3,
                    name = "Silja Björk",
                    bedNumber = 3,
                    room = "103",
                    foodType = vegetarianFood,
                    restrictions = listOf("No nuts")
                ),
                Patient(
                    patientId = 4,
                    name = "Sunna Kristín",
                    bedNumber = 4,
                    room = "104",
                    foodType = normalFood,
                    restrictions = emptyList()
                )
            )

            else -> emptyList()
        }
    }
}