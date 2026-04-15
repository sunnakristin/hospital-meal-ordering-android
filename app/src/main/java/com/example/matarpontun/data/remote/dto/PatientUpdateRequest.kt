package com.example.matarpontun.data.remote.dto

/** Request body for PATCH /patients/{id} — updates a patient's name, food type, and restrictions (US7). */
data class PatientUpdateRequest(
    val name: String,
    val foodTypeName: String,
    val restrictions: List<String>
)
