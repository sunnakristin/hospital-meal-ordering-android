package com.example.matarpontun.domain.model

import com.google.gson.annotations.SerializedName

data class Ward(
    @SerializedName(value = "wardId", alternate = ["id"])
    val id: Long = 0,
    val wardName: String = "",
    val token: String = ""
)