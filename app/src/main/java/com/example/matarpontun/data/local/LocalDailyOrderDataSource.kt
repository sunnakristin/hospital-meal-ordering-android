package com.example.matarpontun.data.local

import com.example.matarpontun.domain.model.DailyOrder
import java.time.LocalDate

interface LocalDailyOrderDataSource {
    suspend fun saveOrder(wardId: Long, order: DailyOrder)
    suspend fun getOrderForPatient(patientId: Long, date: LocalDate): DailyOrder?
    suspend fun getOrdersForWard(wardId: Long, date: LocalDate): List<DailyOrder>
    suspend fun deleteOrderForPatient(patientId: Long, date: LocalDate)
}
