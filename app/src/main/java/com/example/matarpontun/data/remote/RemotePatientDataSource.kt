package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.domain.model.Patient

/**
 * Abstraction over the remote patient API.
 * Implemented by [RemotePatientDataSourceImpl].
 */
interface RemotePatientDataSource {

    /**
     * Fetches all patients for the ward identified by [request] credentials.
     * The backend requires credentials in the body (UC8) rather than a JWT header.
     */
    suspend fun getWardPatients(
        request: LoginRequest
    ): List<Patient>
}
