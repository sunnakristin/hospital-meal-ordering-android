package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.WardOrderResponseDto
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType

/**
 * Abstraction over the remote daily-order API.
 * Implemented by [RemoteDailyOrderDataSourceImpl].
 */
interface RemoteDailyOrderDataSource {

    /** Orders today's meals for a single patient using the given food type. */
    suspend fun orderForPatient(patientId: Long, foodType: FoodType): DailyOrder

    /** Places orders for every patient in the ward in a single request. */
    suspend fun orderForWard(wardId: Long): WardOrderResponseDto
}
