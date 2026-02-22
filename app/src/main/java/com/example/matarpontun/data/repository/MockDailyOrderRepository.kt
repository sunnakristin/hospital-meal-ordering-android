package com.example.matarpontun.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.Meal
import com.example.matarpontun.domain.model.Menu
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.DailyOrderRepository
import java.time.LocalDate
import kotlin.random.Random

class MockDailyOrderRepository : DailyOrderRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createOrderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder> {

        return try {

            val menu = Menu(
                id = 1,
                date = LocalDate.now(),
                foodType = foodType,
                breakfast = Meal(1, "Oatmeal", "", "Breakfast", foodType),
                lunch = Meal(2, "Fish", "", "Lunch", foodType),
                afternoonSnack = Meal(3, "Yogurt", "", "Snack", foodType),
                dinner = Meal(4, "Soup", "", "Dinner", foodType),
                nightSnack = Meal(5, "Sandwich", "", "Snack", foodType)
            )

            val patient = Patient(
                patientId = patientId,          // IMPORTANT: use the parameter
                name = "Mock Patient $patientId",
                bedNumber = 1,
                room = "101",
                foodType = foodType,
                restrictions = emptyList()
            )

            val order = DailyOrder(
                id = Random.nextLong(),
                orderDate = LocalDate.now(),
                status = "CREATED",
                patient = patient,
                menu = menu,
                breakfast = menu.breakfast,
                lunch = menu.lunch,
                afternoonSnack = menu.afternoonSnack,
                dinner = menu.dinner,
                nightSnack = menu.nightSnack
            )

            Result.success(order)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}