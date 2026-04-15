package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.domain.model.Ward

/**
 * Calls the backend ward authentication endpoints and returns [Ward] domain models.
 * Returns null (rather than throwing) on account creation failure so the service
 * layer can produce a user-friendly error message.
 */
class RemoteWardDataSourceImpl(
    private val api: RemoteApiService
) : RemoteWardDataSource {

    override suspend fun login(
        wardName: String,
        password: String
    ): Ward? {
        val request = LoginRequest(
            wardName = wardName,
            password = password
        )
        return api.login(request)
    }

    override suspend fun createAccount(
        wardName: String,
        password: String
    ): Ward? {
        val request = LoginRequest(
            wardName = wardName,
            password = password
        )
        return try {
            api.signUp(request)
        } catch (e: Exception) {
            // Sign-up failures (e.g. duplicate name) are surfaced as null so
            // the service layer can return a meaningful Result.failure message
            null
        }
    }
}
