package com.example.matarpontun.data.remote.dto

data class PatientUpdateRequest(
    val name: String,
    val foodTypeName: String,
    val restrictions: List<String>
)
