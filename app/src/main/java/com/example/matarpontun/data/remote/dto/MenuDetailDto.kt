package com.example.matarpontun.data.remote.dto

/** A single meal slot (one course within a day's menu). */
data class MealSlotDto(
    val name: String,
    val ingredients: String?
)

/** The assigned menuOfTheDay for one food type, as returned by GET /food-types/{id}/menu. */
data class MenuDetailDto(
    val foodTypeId: Long,
    val foodTypeName: String,
    val menuId: Long,
    val breakfast: MealSlotDto?,
    val lunch: MealSlotDto?,
    val afternoonSnack: MealSlotDto?,
    val dinner: MealSlotDto?,
    val nightSnack: MealSlotDto?
)
