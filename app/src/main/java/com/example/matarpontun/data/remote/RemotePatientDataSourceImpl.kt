package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// Matches the JSON you showed from Postman
private data class WardPatientsDto(
    @SerializedName("wardName") val wardName: String,
    @SerializedName("patients") val patients: List<PatientDto>
)

private data class PatientDto(
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("bedNumber") val bedNumber: Int,
    @SerializedName("roomNumber") val roomNumber: String,
    @SerializedName("foodType") val foodTypeCode: String,
    @SerializedName("restrictions") val restrictions: List<String>
)

class RemotePatientDataSourceImpl(
    private val api: RemoteApiService
) : RemotePatientDataSource {

    override suspend fun getWardPatients(
        request: LoginRequest
    ): List<Patient> {

        val dto = api.getWardPatients(request)

        return dto.patients.map { p ->
            Patient(
                patientId = p.patientId,
                name = p.name,
                bedNumber = p.bedNumber,
                room = p.roomNumber,
                foodType = FoodType(typeName = p.foodType),
                restrictions = p.restrictions
            )
        }
    }
}