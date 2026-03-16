package com.example.matarpontun.data.remote

import com.example.matarpontun.domain.model.Ward

interface RemoteWardDataSource {
    suspend fun login(wardName: String, password: String): Ward?
}