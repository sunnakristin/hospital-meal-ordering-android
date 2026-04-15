package com.example.matarpontun.data.remote.dto

/** Request body for PUT /wards/{id} — updates the ward's name and password (US5). */
data class WardUpdateRequest(
    val wardName: String,
    val password: String
)
