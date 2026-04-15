package com.example.matarpontun.domain.model

/**
 * A single meal item (e.g. "Chicken soup") belonging to a food type.
 * Meals are assigned to slots (breakfast, lunch, etc.) within a [Menu].
 */
data class Meal(
    val id: Long,
    val name: String,
    val ingredients: String,
    val category: String,   // e.g. "Breakfast", "Lunch"
    val foodType: FoodType
)
