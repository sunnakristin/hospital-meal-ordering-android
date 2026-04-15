package com.example.matarpontun.domain.model

import java.time.LocalDate

/**
 * The menu of the day for a given [foodType].
 * Each food type has one menu assigned per day, containing five meal slots.
 */
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
