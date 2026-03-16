package com.example.matarpontun.data.remote.dto

/**
 * Matches backend UC1 "orderFoodForPatient" request body:
 * { "foodType": "A1" }
 */
data class OrderRequest(
    val foodType: String
)

