package com.example.matarpontun.domain.repository

import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient

interface DailyOrderRepository {
    suspend fun createOrderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder>

    suspend fun createOrdersForWard(
        wardId: Long,
        patients: List<Patient> // þetta er útaf enn mock repository munum nota POST /{wardId}/order
    ): Result<List<DailyOrder>>

    suspend fun getDailyOrdersForWard(
        wardId: Long
    ): Result<List<DailyOrder>>

    suspend fun fixConflicts(
        patientId: Long
    ): Result<DailyOrder>
}