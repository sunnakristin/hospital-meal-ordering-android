package com.example.matarpontun.domain.repository

import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType

interface DailyOrderRepository {

    suspend fun orderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder>

    suspend fun orderForWard(
        wardId: Long
    ):  Result<Unit>
}