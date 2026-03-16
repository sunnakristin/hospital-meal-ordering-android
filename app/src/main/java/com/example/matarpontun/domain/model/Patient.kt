package com.example.matarpontun.domain.model

data class Patient (
    val patientId: Long,
    val name: String,
    val bedNumber: Int,
    val room: String, //Room,
    val foodType: FoodType,
    val restrictions: List<String>,
    val status: String,

    // kemur frá bakenda
    val breakfast: String? = null,
    val lunch: String? = null,
    val afternoonSnack: String? = null,
    val dinner: String? = null,
    val nightSnack: String? = null
)