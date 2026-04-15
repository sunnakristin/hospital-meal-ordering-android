package com.example.matarpontun.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Response from POST /patients/all — the ward name and all its patients. */
data class WardPatientsDto(
    val wardName: String,
    val patients: List<PatientDto>
)

/** JSON representation of a single patient as returned by the backend. */
data class PatientDto(

    @SerializedName("patientId")
    val patientId: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("bedNumber")
    val bedNumber: Int,

    @SerializedName("roomNumber")
    val roomNumber: String,

    @SerializedName("roomQrCode")
    val roomQrCode: String?,

    @SerializedName("foodType")
    val foodType: String,

    @SerializedName("restrictions")
    val restrictions: List<String>,

    /** "OK", "AUTO CHANGED", "NEEDS MANUAL CHANGE", or null if no order exists yet today. */
    @SerializedName("status")
    val status: String?,

    /** Today's meal names — present only when a daily order exists for this patient. */
    @SerializedName("meals")
    val meals: MealsDto?
)
