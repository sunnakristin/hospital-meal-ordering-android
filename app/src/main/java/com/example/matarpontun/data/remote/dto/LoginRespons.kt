package com.example.matarpontun.data.remote.dto

data class LoginResponse(
    val token: String,
    val wardName: String,
    val wardId: Long,
    val message: String
)