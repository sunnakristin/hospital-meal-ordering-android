package com.example.matarpontun.data.repository

import com.example.matarpontun.data.remote.RemoteWardDataSource
import com.example.matarpontun.domain.model.Ward
import com.example.matarpontun.domain.repository.WardRepository

class NetworkWardRepository(
    private val remoteDataSource: RemoteWardDataSource
) : WardRepository {

    override suspend fun login(wardName: String, password: String): Ward? {
        return remoteDataSource.login(wardName, password)
    }
}