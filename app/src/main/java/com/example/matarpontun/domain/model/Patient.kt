package com.example.matarpontun.domain.model

/**
 * Core domain model representing a hospital patient.
 *
 * @property patientId Unique backend ID.
 * @property room Room number the patient is assigned to.
 * @property roomQrCode QR code string for the patient's room, used for scanning (US11).
 * @property foodType The patient's assigned dietary category (e.g. Regular, Diabetic).
 * @property restrictions List of dietary restrictions (e.g. "gluten", "dairy").
 * @property status Backend order status — "OK", "AUTO CHANGED", "NEEDS MANUAL CHANGE", or empty.
 * @property breakfast/lunch/etc Today's assigned meal names, present when an order exists for today.
 */
data class Patient(
    val patientId: Long,
    val name: String,
    val bedNumber: Int,
    val room: String,
    val roomQrCode: String?,
    val foodType: FoodType,
    val restrictions: List<String>,
    val status: String,

    // Today's meal names — included in the patient response when an order has been placed
    val breakfast: String? = null,
    val lunch: String? = null,
    val afternoonSnack: String? = null,
    val dinner: String? = null,
    val nightSnack: String? = null
)
