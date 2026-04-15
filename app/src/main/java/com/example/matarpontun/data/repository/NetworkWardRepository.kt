package com.example.matarpontun.data.repository

import com.example.matarpontun.data.remote.RemoteWardDataSource
import com.example.matarpontun.domain.model.Ward
import com.example.matarpontun.domain.repository.WardRepository

/**
 * Network-only implementation of [WardRepository].
 * Ward login and account creation have no offline fallback — the user must be
 * connected to authenticate.
 */
class NetworkWardRepository(
    private val remoteDataSource: RemoteWardDataSource
) : WardRepository {

    override suspend fun login(wardName: String, password: String): Ward? {
        return remoteDataSource.login(wardName, password)
    }

    override suspend fun createAccount(wardName: String, password: String): Ward? {
        return remoteDataSource.createAccount(wardName, password)
    }
}
