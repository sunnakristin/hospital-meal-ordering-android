package com.example.matarpontun.data.remote.dto

/** Response from POST /wards/{id}/order. */
data class WardOrderResponseDto(
    val message: String,
    val ward: String,
    /** One entry per patient that had at least one meal-slot conflict. Empty if no conflicts. */
    val conflicts: List<PatientConflictDto>? = null
)

/** Per-patient conflict summary within a ward order response. */
data class PatientConflictDto(
    val patientName: String,
    val patientId: Long,
    val conflicts: List<ConflictDto>,
    val status: String
)
