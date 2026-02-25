package com.example.matarpontun.domain.model

import java.time.LocalDate

data class Menu(
    val id: Long,
    val date: LocalDate,
    val foodType: FoodType,
    val breakfast: Meal,
    val afternoonSnack: Meal,
    val lunch: Meal,
    val dinner: Meal,
    val nightSnack: Meal
)
