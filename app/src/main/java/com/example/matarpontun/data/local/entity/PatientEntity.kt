package com.example.matarpontun.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a cached patient.
 * Scoped to a ward via [wardId] so patients from different wards don't mix.
 * Restrictions are stored as a comma-separated string because Room cannot store List<String> directly.
 * Today's meal names are nullable — they are only present when an order has already been placed.
 */
@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val patientId: Long,
    val wardId: Long,
    val name: String,
    val bedNumber: Int,
    val room: String,
    val roomQrCode: String?,
    val foodTypeName: String,
    val restrictions: String,   // comma-separated, e.g. "gluten,dairy"
    val status: String,         // "OK", "AUTO CHANGED", "NEEDS MANUAL CHANGE", or ""
    val breakfast: String?,
    val lunch: String?,
    val afternoonSnack: String?,
    val dinner: String?,
    val nightSnack: String?
)
