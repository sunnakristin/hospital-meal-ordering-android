package com.example.matarpontun.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Domain model representing a ward account.
 * The [SerializedName] annotation handles the fact that the login response uses "wardId"
 * while other endpoints use "id" for the same field.
 */
data class Ward(
    @SerializedName(value = "wardId", alternate = ["id"])
    val id: Long = 0,
    val wardName: String = "",
    val token: String = ""
)
