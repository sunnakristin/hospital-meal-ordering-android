package com.example.matarpontun.domain.model

import com.google.gson.annotations.SerializedName

data class Ward(
    @SerializedName("wardId")
    val id: Long,
    val wardName: String,
    val token: String
    //val patients: List<Patient>
)