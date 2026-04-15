package com.example.matarpontun.data.local

import com.example.matarpontun.domain.model.DailyOrder
import java.time.LocalDate

/**
 * Abstraction over the local Room database for daily orders.
 * Implemented by [LocalDailyOrderDataSourceImpl].
 */
interface LocalDailyOrderDataSource {
    /** Inserts or replaces the order for the given ward. */
    suspend fun saveOrder(wardId: Long, order: DailyOrder)

    /** Returns the cached order for [patientId] on [date], or null if none exists. */
    suspend fun getOrderForPatient(patientId: Long, date: LocalDate): DailyOrder?

    /** Returns all cached orders for [wardId] on [date]. */
    suspend fun getOrdersForWard(wardId: Long, date: LocalDate): List<DailyOrder>

    /** Removes the cached order for [patientId] on [date]. */
    suspend fun deleteOrderForPatient(patientId: Long, date: LocalDate)
}
