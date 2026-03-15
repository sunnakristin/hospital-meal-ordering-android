package com.example.matarpontun.data.remote

import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType

interface RemoteDailyOrderDataSource {

    // kallar á POST /patients/{id}/order
    suspend fun orderForPatient(
        patientId: Long,
        foodType: FoodType
    ): DailyOrder
    suspend fun orderForWard(
        wardId: Long
    )
}