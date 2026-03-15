package com.example.matarpontun.domain.repository

import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient

interface DailyOrderRepository {

    suspend fun orderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder>

    suspend fun orderForWard(
        wardId: Long
    ):  Result<List<DailyOrder>>//Result<Unit>
}