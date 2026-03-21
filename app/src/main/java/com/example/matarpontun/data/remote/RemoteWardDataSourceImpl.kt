package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.domain.model.Ward

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
            null
        }
    }
}
