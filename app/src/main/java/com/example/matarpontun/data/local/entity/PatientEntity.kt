package com.example.matarpontun.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val patientId: Long,
    val wardId: Long,
    val name: String,
    val bedNumber: Int,
    val room: String,
    val roomQrCode: String?,
    val foodTypeName: String,
    val restrictions: String, // comma-separated
    val status: String,
    val breakfast: String?,
    val lunch: String?,
    val afternoonSnack: String?,
    val dinner: String?,
    val nightSnack: String?
)
