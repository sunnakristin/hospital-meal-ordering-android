package com.example.matarpontun.data.remote.dto

data class RoomCreatedResponse(
    val roomId: Long,
    val roomNumber: String,
    val maxPatients: Int,
    val qrCode: String,
    val patients: List<PatientSummaryDto>
)

data class PatientSummaryDto(
    val id: Long,
    val name: String,
    val age: Int,
    val bedNumber: Int,
    val foodType: String
)
