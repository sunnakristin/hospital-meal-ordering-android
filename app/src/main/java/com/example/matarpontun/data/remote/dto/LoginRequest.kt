package com.example.matarpontun.data.remote.dto

/**
 * Request body for ward sign-in (POST /wards/signIn) and patient fetch (POST /patients/all).
 * Stored in [AppContainer.currentLoginRequest] for the session lifetime so the patient
 * endpoint can be called without re-entering credentials.
 */
data class LoginRequest(
    val wardName: String,
    val password: String
)
