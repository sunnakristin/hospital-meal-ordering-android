package com.example.matarpontun.data.remote.dto

data class WardPatientsDto(
    val wardName: String,
    val patients: List<PatientDto>
)

data class PatientDto(
    val patientId: Long,
    val name: String,
    val bedNumber: Int,
    val roomNumber: String,
    val foodType: String,
    val restrictions: List<String>,
    val status: String
)