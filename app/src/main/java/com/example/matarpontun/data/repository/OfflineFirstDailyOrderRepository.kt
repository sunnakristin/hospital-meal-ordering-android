package com.example.matarpontun.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.matarpontun.data.local.LocalDailyOrderDataSource
import com.example.matarpontun.data.remote.RemoteDailyOrderDataSource
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.repository.DailyOrderRepository
import java.time.LocalDate

/**
 * Repository that calls the remote API first and caches the result locally.
 * If the remote call fails, it falls back to the local cache for today's date.
 *
 * wardId must be provided so orders can be stored and retrieved per-ward.
 */
class OfflineFirstDailyOrderRepository(
    private val remoteDataSource: RemoteDailyOrderDataSource,
    private val localDataSource: LocalDailyOrderDataSource,
    private val wardId: () -> Long   // lambda so it always reads the current value from AppContainer
) : DailyOrderRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun orderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder> {
        return try {
            val order = remoteDataSource.orderForPatient(patientId, foodType)
            localDataSource.saveOrder(wardId(), order)
            Result.success(order)
        } catch (e: Exception) {
            // Remote failed — try local cache for today
            val cached = localDataSource.getOrderForPatient(patientId, LocalDate.now())
            if (cached != null) Result.success(cached)
            else Result.failure(e)
        }
    }

    override suspend fun orderForWard(wardId: Long): Result<Unit> {
        return try {
            remoteDataSource.orderForWard(wardId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
