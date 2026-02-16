package com.example.matarpontun.data.repository

import com.example.matarpontun.domain.model.Ward
import com.example.matarpontun.domain.repository.WardRepository

class MockWardRepository: WardRepository {
    override suspend fun login(wardName: String, password: String): Ward? {
        // Mock implementation
        return if (wardName == "test" && password == "password") {
            Ward(1, "Test Ward", token = "mock-jwt-token")
        } else {
            return null
        }
    }
}