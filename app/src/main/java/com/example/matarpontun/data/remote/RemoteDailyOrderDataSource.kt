package com.example.matarpontun.data.remote

import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType

interface RemoteDailyOrderDataSource {
    suspend fun createOrderForPatient(
        patientId: Long,
        foodType: FoodType
    ): DailyOrder
}