package com.example.matarpontun.domain.model

data class Patient (
    val patientId: Long,
    val name: String,
    val bedNumber: Int,
    val room: String, //Room,
    val foodType: FoodType,
    val restrictions: List<String>
)