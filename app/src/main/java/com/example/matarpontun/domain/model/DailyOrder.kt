package com.example.matarpontun.domain.model

import java.time.LocalDate

data class DailyOrder(
    val id: Long,
    val orderDate: LocalDate,
    val status: String,
    val patient: Patient,
    val menu: Menu,
    val breakfast: Meal,
    val lunch: Meal,
    val afternoonSnack: Meal,
    val dinner: Meal,
    val nightSnack: Meal
)
