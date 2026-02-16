package com.example.matarpontun.domain.repository

import com.example.matarpontun.domain.model.Ward

interface WardRepository {
    suspend fun login(wardName: String, password: String): Ward?
}