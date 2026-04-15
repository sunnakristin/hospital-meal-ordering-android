package com.example.matarpontun.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Today's meal names embedded in a [PatientDto].
 * Only present when the patient already has a [DailyOrder] for today.
 * All fields are nullable because the backend may omit them if a slot has no meal assigned.
 */
data class MealsDto(

    @SerializedName("breakfastName")
    val breakfastName: String?,

    @SerializedName("lunchName")
    val lunchName: String?,

    @SerializedName("afternoonSnackName")
    val afternoonSnackName: String?,

    @SerializedName("dinnerName")
    val dinnerName: String?,

    @SerializedName("nightSnackName")
    val nightSnackName: String?
)
