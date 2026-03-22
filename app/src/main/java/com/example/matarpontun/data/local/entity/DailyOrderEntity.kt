package com.example.matarpontun.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_orders")
data class DailyOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wardId: Long,
    val patientId: Long,
    val patientName: String,
    val orderDate: String,       // ISO "yyyy-MM-dd"
    val status: String,
    val foodTypeName: String,
    val breakfastName: String,
    val lunchName: String,
    val afternoonSnackName: String,
    val dinnerName: String,
    val nightSnackName: String
)
