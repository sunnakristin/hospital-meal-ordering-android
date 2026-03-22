package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.data.remote.dto.WardPatientsDto
import com.example.matarpontun.data.remote.dto.PatientDto
import com.example.matarpontun.data.remote.dto.MealsDto
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient
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
                roomQrCode = p.roomQrCode,
                foodType = FoodType(typeName = p.foodType),
                restrictions = p.restrictions,
                status = p.status ?: "N/A",

                breakfast = p.meals?.breakfastName,
                lunch = p.meals?.lunchName,
                afternoonSnack = p.meals?.afternoonSnackName,
                dinner = p.meals?.dinnerName,
                nightSnack = p.meals?.nightSnackName

            )
        }
    }
}