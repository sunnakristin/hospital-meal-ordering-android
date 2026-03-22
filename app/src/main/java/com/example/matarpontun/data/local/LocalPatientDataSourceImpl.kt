package com.example.matarpontun.data.local

import com.example.matarpontun.data.local.dao.PatientDao
import com.example.matarpontun.data.local.entity.PatientEntity
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient

class LocalPatientDataSourceImpl(
    private val dao: PatientDao
) {
    suspend fun savePatients(wardId: Long, patients: List<Patient>) {
        dao.insertAll(patients.map { it.toEntity(wardId) })
    }

    suspend fun getPatientsByWard(wardId: Long): List<Patient> =
        dao.getPatientsByWard(wardId).map { it.toDomain() }
}

private fun Patient.toEntity(wardId: Long) = PatientEntity(
    patientId = patientId,
    wardId = wardId,
    name = name,
    bedNumber = bedNumber,
    room = room,
    roomQrCode = roomQrCode,
    foodTypeName = foodType.typeName,
    restrictions = restrictions.joinToString(","),
    status = status,
    breakfast = breakfast,
    lunch = lunch,
    afternoonSnack = afternoonSnack,
    dinner = dinner,
    nightSnack = nightSnack
)

private fun PatientEntity.toDomain() = Patient(
    patientId = patientId,
    name = name,
    bedNumber = bedNumber,
    room = room,
    roomQrCode = roomQrCode,
    foodType = FoodType(foodTypeName),
    restrictions = if (restrictions.isBlank()) emptyList() else restrictions.split(","),
    status = status,
    breakfast = breakfast,
    lunch = lunch,
    afternoonSnack = afternoonSnack,
    dinner = dinner,
    nightSnack = nightSnack
)
