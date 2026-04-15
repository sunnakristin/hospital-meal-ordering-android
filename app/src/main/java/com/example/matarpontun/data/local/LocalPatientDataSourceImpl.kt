package com.example.matarpontun.data.local

import com.example.matarpontun.data.local.dao.PatientDao
import com.example.matarpontun.data.local.entity.PatientEntity
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient

/**
 * Room-backed local cache for patients.
 * Converts between [Patient] domain models and [PatientEntity] Room entities.
 * Restrictions are stored as a comma-separated string since Room doesn't support List<String> natively.
 */
class LocalPatientDataSourceImpl(
    private val dao: PatientDao
) {
    /** Replaces all cached patients for the ward (REPLACE conflict strategy on the DAO). */
    suspend fun savePatients(wardId: Long, patients: List<Patient>) {
        dao.insertAll(patients.map { it.toEntity(wardId) })
    }

    /** Returns all cached patients for [wardId], or an empty list if none are cached. */
    suspend fun getPatientsByWard(wardId: Long): List<Patient> =
        dao.getPatientsByWard(wardId).map { it.toDomain() }
}

/** Maps a [Patient] domain model to a flat [PatientEntity] for Room storage. */
private fun Patient.toEntity(wardId: Long) = PatientEntity(
    patientId = patientId,
    wardId = wardId,
    name = name,
    bedNumber = bedNumber,
    room = room,
    roomQrCode = roomQrCode,
    foodTypeName = foodType.typeName,
    restrictions = restrictions.joinToString(","),  // stored as comma-separated string
    status = status,
    breakfast = breakfast,
    lunch = lunch,
    afternoonSnack = afternoonSnack,
    dinner = dinner,
    nightSnack = nightSnack
)

/** Reconstructs a [Patient] domain model from a cached [PatientEntity]. */
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
