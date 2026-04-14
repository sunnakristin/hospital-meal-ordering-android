// OrderResponseDto.kt
package com.example.matarpontun.data.remote.dto

data class OrderResponseDto(
    val status: String,
    val message: String,
    val meals: OrderMealsDto,
    val patientId: Long,
    val foodType: String,
    val orderDate: String,
    /** Populated when the order status is AUTO CHANGED or NEEDS MANUAL CHANGE. */
    val conflicts: List<ConflictDto>? = null
)

/** A single meal-slot conflict returned by the order endpoint. */
data class ConflictDto(
    val slot: String,
    val originalMeal: String,
    val matchedRestriction: String,
    val replacementMeal: String?  // null = manual change required
)

data class OrderMealsDto(
    val nightSnack: String,
    val lunch: String,
    val breakfast: String,
    val afternoonSnack: String,
    val dinner: String
)
