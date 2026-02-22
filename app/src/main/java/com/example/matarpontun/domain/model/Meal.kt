package com.example.matarpontun.domain.model

data class Meal(
    val id: Long,
    val name: String,
    val ingredients: String, //List<String>,
    val category: String,
    val foodType: FoodType
)
