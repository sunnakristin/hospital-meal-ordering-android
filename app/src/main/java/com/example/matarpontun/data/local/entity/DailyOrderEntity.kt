package com.example.matarpontun.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a cached daily order.
 * Stores only the meal names (not full Meal objects) since that is all the UI needs offline.
 * Dates are stored as ISO strings ("yyyy-MM-dd") because Room has no native LocalDate type.
 */
@Entity(tableName = "daily_orders")
data class DailyOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wardId: Long,
    val patientId: Long,
    val patientName: String,
    val orderDate: String,          // ISO "yyyy-MM-dd"
    val status: String,             // "OK", "AUTO CHANGED", or "NEEDS MANUAL CHANGE"
    val foodTypeName: String,
    val breakfastName: String,
    val lunchName: String,
    val afternoonSnackName: String,
    val dinnerName: String,
    val nightSnackName: String
)
