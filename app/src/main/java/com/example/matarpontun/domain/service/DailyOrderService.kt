package com.example.matarpontun.domain.service

import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.repository.DailyOrderRepository

class DailyOrderService(
    private val repository: DailyOrderRepository
) {

    suspend fun createOrderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder> {

        if (foodType.typeName.isBlank()) {
            return Result.failure(IllegalArgumentException("Invalid food type"))
        }

        return repository.createOrderForPatient(patientId, foodType)
    }
}