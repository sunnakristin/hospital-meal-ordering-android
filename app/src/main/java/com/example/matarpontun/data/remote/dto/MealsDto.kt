package com.example.matarpontun.data.remote.dto

import com.google.gson.annotations.SerializedName

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