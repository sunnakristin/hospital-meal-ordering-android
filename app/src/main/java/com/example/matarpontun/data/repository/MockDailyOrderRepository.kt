package com.example.matarpontun.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.matarpontun.domain.model.*
import com.example.matarpontun.domain.repository.DailyOrderRepository
import java.time.LocalDate
import kotlin.random.Random

class MockDailyOrderRepository : DailyOrderRepository {

    private val ordersByWard: MutableMap<Long, MutableMap<Long, DailyOrder>> = mutableMapOf()
    private val wardId = 1L // hardcoded - for now

    // creates order for single patient
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createOrderForPatient(
        patientId: Long,
        foodType: FoodType
    ): Result<DailyOrder> {

        return try {

            val today = LocalDate.now()
            val wardOrders = ordersByWard.getOrPut(wardId) { mutableMapOf() }
            val menu = createMenuForFoodType(foodType)

            val patient = Patient(
                patientId = patientId,
                name = "Mock Patient $patientId",
                bedNumber = 1,
                room = "101",
                foodType = foodType,
                restrictions = listOf("milk")//emptyList()
            )

            var order = DailyOrder(
                id = Random.nextLong(),
                orderDate = today,
                status = "SUBMITTED",
                patient = patient,
                menu = menu,
                breakfast = menu.breakfast,
                lunch = menu.lunch,
                afternoonSnack = menu.afternoonSnack,
                dinner = menu.dinner,
                nightSnack = menu.nightSnack
            )

            // Simulate backend restriction check
            order = simulateRestrictionCheck(order)

            wardOrders[patientId] = order

            Result.success(order)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

   // creates orders for all patients in ward
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createOrdersForWard(
        wardId: Long,
        patients: List<Patient>
    ): Result<List<DailyOrder>> {

        val results = patients.mapNotNull { patient ->
            createOrderForPatient(patient.patientId, patient.foodType)
                .getOrNull()
        }

        return Result.success(results)
    }

    // get today orders for ward
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getDailyOrdersForWard(
        wardId: Long
    ): Result<List<DailyOrder>> {

        val today = LocalDate.now()
        val wardOrders = ordersByWard[wardId] ?: emptyMap()

        return Result.success(
            wardOrders.values.filter { it.orderDate == today }
        )
    }

    // fix conflicts for a single patient (re-run restriction check)
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fixConflicts(patientId: Long): Result<DailyOrder> {

        val wardOrders = ordersByWard[wardId]
            ?: return Result.failure(IllegalStateException("No orders found"))

        val existingOrder = wardOrders[patientId]
            ?: return Result.failure(IllegalStateException("No order for patient"))

        val updatedOrder = simulateRestrictionCheck(existingOrder)

        wardOrders[patientId] = updatedOrder

        return Result.success(updatedOrder)
    }

    // creates menus
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMenuForFoodType(foodType: FoodType): Menu {

        val today = LocalDate.now()

        return when (foodType.typeName) {

            "A1" -> Menu(
                id = 1,
                date = today,
                foodType = foodType,
                breakfast = Meal(1, "Oatmeal", "milk", "Breakfast", foodType),
                lunch = Meal(2, "Fish", "fish", "Lunch", foodType),
                afternoonSnack = Meal(3, "Yogurt", "milk", "Snack", foodType),
                dinner = Meal(4, "Soup", "vegetables", "Dinner", foodType),
                nightSnack = Meal(5, "Sandwich", "gluten", "Snack", foodType)
            )

            "A2" -> Menu(
                id = 2,
                date = today,
                foodType = foodType,
                breakfast = Meal(6, "Eggs", "eggs", "Breakfast", foodType),
                lunch = Meal(7, "Chicken", "chicken", "Lunch", foodType),
                afternoonSnack = Meal(8, "Fruit", "fruit", "Snack", foodType),
                dinner = Meal(9, "Stew", "meat", "Dinner", foodType),
                nightSnack = Meal(10, "Crackers", "gluten", "Snack", foodType)
            )

            else -> throw IllegalArgumentException("Unknown food type")
        }
    }

    // simulated backend restriction logic
    private fun simulateRestrictionCheck(order: DailyOrder): DailyOrder {

        val restrictions = order.patient.restrictions.map { it.lowercase() }

        var autoChanged = false
        var needsManual = false

        fun hasConflict(meal: Meal?): Boolean {
            if (meal == null) return false
            return restrictions.any { meal.ingredients.lowercase().contains(it) }
        }

        fun safeReplacement(category: String): Meal? {
            return Meal(
                id = Random.nextLong(),
                name = "Safe $category",
                ingredients = "",
                category = category,
                foodType = order.patient.foodType
            )
        }

        if (hasConflict(order.breakfast)) {
            val replacement = safeReplacement("Breakfast")
            if (replacement != null) {
                order.breakfast = replacement
                autoChanged = true
            } else {
                needsManual = true
            }
        }

        if (hasConflict(order.lunch)) {
            val replacement = safeReplacement("Lunch")
            if (replacement != null) {
                order.lunch = replacement
                autoChanged = true
            } else {
                needsManual = true
            }
        }

        if (hasConflict(order.afternoonSnack)) {
            val replacement = safeReplacement("Snack")
            if (replacement != null) {
                order.afternoonSnack = replacement
                autoChanged = true
            } else {
                needsManual = true
            }
        }

        if (hasConflict(order.dinner)) {
            val replacement = safeReplacement("Dinner")
            if (replacement != null) {
                order.dinner = replacement
                autoChanged = true
            } else {
                needsManual = true
            }
        }

        if (hasConflict(order.nightSnack)) {
            val replacement = safeReplacement("Snack")
            if (replacement != null) {
                order.nightSnack = replacement
                autoChanged = true
            } else {
                needsManual = true
            }
        }

        order.status = when {
            needsManual -> "NEEDS MANUAL CHANGE"
            autoChanged -> "AUTO CHANGED"
            else -> "SUBMITTED"
        }

        return order
    }
}