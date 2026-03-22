package com.example.matarpontun.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WardPatientsDto(
    val wardName: String,
    val patients: List<PatientDto>
)

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

    @SerializedName("status")
    val status: String?,

    @SerializedName("meals")
    val meals: MealsDto?

)