package com.example.matarpontun.domain.service

import com.example.matarpontun.data.remote.dto.WardOrderResponseDto
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.repository.DailyOrderRepository

/**
 * Domain service for meal ordering operations.
 * Validates inputs before delegating to [DailyOrderRepository].
 */
class DailyOrderService(
    private val repository: DailyOrderRepository
) {

    /**
     * Places today's meal order for a single patient using their assigned [foodType].
     * Returns [Result.failure] if [foodType] is blank (guard against ordering with no food type set).
     */
    suspend fun orderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder> {

        if (foodType.typeName.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Invalid food type")
            )
        }

        return repository.orderForPatient(patientId, foodType)
    }

    /** Places orders for every patient in the ward in a single batch request. */
    suspend fun orderForWard(wardId: Long): Result<WardOrderResponseDto> {
        return repository.orderForWard(wardId)
    }
}
