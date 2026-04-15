package com.example.matarpontun.data.remote.dto

/** Request body for POST /wards/{wardId}/rooms — creates a new room with auto-generated patients. */
data class RoomCreateRequest(
    val roomNumber: String,
    val maxPatients: Int
)
