package com.example.matarpontun.domain.model

import java.time.LocalDate

data class DailyOrder(
    var id: Long,
    var orderDate: LocalDate,
    var status: String,
    var patient: Patient,
    var menu: Menu,
    var breakfast: Meal,
    var lunch: Meal,
    var afternoonSnack: Meal,
    var dinner: Meal,
    var nightSnack: Meal
)
