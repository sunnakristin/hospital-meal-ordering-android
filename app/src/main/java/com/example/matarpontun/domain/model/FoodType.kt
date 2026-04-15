package com.example.matarpontun.domain.model

/**
 * Represents a dietary category assigned to a patient (e.g. "Regular", "Diabetic", "Vegetarian").
 * Each food type has a separate [Menu] assigned by the kitchen for each day.
 */
data class FoodType(
    val typeName: String
)
