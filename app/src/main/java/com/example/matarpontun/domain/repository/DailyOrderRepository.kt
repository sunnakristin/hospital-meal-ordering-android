package com.example.matarpontun.domain.repository

import com.example.matarpontun.data.remote.dto.WardOrderResponseDto
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType

/**
 * Domain interface for daily order operations.
 * Implemented by [OfflineFirstDailyOrderRepository] (with Room fallback)
 * and [NetworkDailyOrderRepository] (network only).
 */
interface DailyOrderRepository {

    /** Places a meal order for a single patient. Returns the created [DailyOrder] or a failure. */
    suspend fun orderForPatient(patientId: Long, foodType: FoodType): Result<DailyOrder>

    /** Places meal orders for every patient in the ward in one batch request. */
    suspend fun orderForWard(wardId: Long): Result<WardOrderResponseDto>
}
