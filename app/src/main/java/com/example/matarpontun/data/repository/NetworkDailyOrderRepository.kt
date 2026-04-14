package com.example.matarpontun.data.repository

import android.util.Log
import com.example.matarpontun.data.remote.RemoteDailyOrderDataSource
import com.example.matarpontun.data.remote.dto.WardOrderResponseDto
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.repository.DailyOrderRepository

class NetworkDailyOrderRepository(
    private val remoteDataSource: RemoteDailyOrderDataSource
) : DailyOrderRepository {

    override suspend fun orderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder> {
        return try {
            Result.success(
                remoteDataSource.orderForPatient(patientId, foodType)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun orderForWard(wardId: Long): Result<WardOrderResponseDto> {
        return try {
            Log.d("WARD_ORDER", "Ordering ward $wardId")
            Result.success(remoteDataSource.orderForWard(wardId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}