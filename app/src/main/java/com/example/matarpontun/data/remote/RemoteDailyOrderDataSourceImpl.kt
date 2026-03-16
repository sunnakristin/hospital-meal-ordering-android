// RemoteDailyOrderDataSourceImpl.kt
package com.example.matarpontun.data.remote

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.matarpontun.data.remote.dto.OrderRequest
import com.example.matarpontun.domain.model.*
import java.time.LocalDate

class RemoteDailyOrderDataSourceImpl(
    private val api: RemoteApiService
) : RemoteDailyOrderDataSource {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun orderForPatient(
        patientId: Long,
        foodType: FoodType
    ): DailyOrder {
        val request = OrderRequest(foodType = foodType.typeName)
        val dto = api.createOrder(patientId, request)

        val date = LocalDate.parse(dto.orderDate)

        fun mealFrom(name: String, category: String): Meal =
            Meal(
                id = 0L,
                name = name,
                ingredients = "",
                category = category,
                foodType = foodType
            )

        val menu = Menu(
            id = 0L,
            date = date,
            foodType = foodType,
            breakfast = mealFrom(dto.meals.breakfast, "Breakfast"),
            afternoonSnack = mealFrom(dto.meals.afternoonSnack, "Afternoon snack"),
            lunch = mealFrom(dto.meals.lunch, "Lunch"),
            dinner = mealFrom(dto.meals.dinner, "Dinner"),
            nightSnack = mealFrom(dto.meals.nightSnack, "Night snack")
        )

        // Minimal patient – PatientListViewModel already knows the real patient
        val patient = Patient(
            patientId = dto.patientId,
            name = "",
            bedNumber = 0,
            room = "",
            foodType = foodType,
            restrictions = emptyList(),
            status = dto.status
        )

        return DailyOrder(
            id = 0L,
            orderDate = date,
            status = dto.status,
            patient = patient,
            menu = menu,
            breakfast = menu.breakfast,
            lunch = menu.lunch,
            afternoonSnack = menu.afternoonSnack,
            dinner = menu.dinner,
            nightSnack = menu.nightSnack
        )
    }

    override suspend fun orderForWard(wardId: Long) {
        api.orderWard(wardId)
    }
}