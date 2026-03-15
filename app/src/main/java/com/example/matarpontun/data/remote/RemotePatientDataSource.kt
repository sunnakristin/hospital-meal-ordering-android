package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.domain.model.Patient

interface RemotePatientDataSource {

    suspend fun getWardPatients(
        request: LoginRequest
    ): List<Patient>
}