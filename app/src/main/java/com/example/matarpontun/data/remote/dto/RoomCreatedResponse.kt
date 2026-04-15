package com.example.matarpontun.data.remote.dto

/** Response from POST /wards/{wardId}/rooms — the created room with its generated patients. */
data class RoomCreatedResponse(
    val roomId: Long,
    val roomNumber: String,
    val maxPatients: Int,
    /** QR code string assigned to this room by the backend, used for US11 scanning. */
    val qrCode: String,
    val patients: List<PatientSummaryDto>
)

/** Minimal patient stub returned inside room creation and fill-patient dialogs. */
data class PatientSummaryDto(
    val id: Long,
    val name: String,
    val age: Int,
    val bedNumber: Int,
    val foodType: String
)
