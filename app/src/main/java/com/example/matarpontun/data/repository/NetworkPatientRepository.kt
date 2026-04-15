package com.example.matarpontun.data.repository

import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.remote.RemotePatientDataSource
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository

/**
 * Network-only implementation of [PatientRepository].
 * Used as a fallback when offline support is not required.
 *
 * The backend UC8 endpoint expects ward credentials in the request body (not a JWT header),
 * so this repository reads the last successful login from [AppContainer.currentLoginRequest].
 */
class NetworkPatientRepository(
    private val remoteDataSource: RemotePatientDataSource
) : PatientRepository {

    override suspend fun getPatientsByWard(wardId: Long): List<Patient> {
        val loginRequest = AppContainer.currentLoginRequest
            ?: throw IllegalStateException("No ward login information available")

        return remoteDataSource.getWardPatients(loginRequest)
    }
}
