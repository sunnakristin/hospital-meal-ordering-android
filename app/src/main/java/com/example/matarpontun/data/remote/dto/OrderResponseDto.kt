// OrderResponseDto.kt
package com.example.matarpontun.data.remote.dto

data class OrderResponseDto(
    val status: String,
    val message: String,
    val meals: MealsDto,
    val patientId: Long,
    val foodType: String,
    val orderDate: String
)

data class MealsDto(
    val nightSnack: String,
    val lunch: String,
    val breakfast: String,
    val afternoonSnack: String,
    val dinner: String
)