package com.example.matarpontun.data.repository

import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.remote.RemotePatientDataSource
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository

class NetworkPatientRepository(
    private val remoteDataSource: RemotePatientDataSource
) : PatientRepository {

    override suspend fun getPatientsByWard(wardId: Long): List<Patient> {
        // Backend UC8 expects ward credentials (WardDTO/LoginRequest) in the body.
        // We reuse the last successful login credentials stored in AppContainer.
        val loginRequest = AppContainer.currentLoginRequest
            ?: throw IllegalStateException("No ward login information available")

        return remoteDataSource.getWardPatients(loginRequest)
    }
}