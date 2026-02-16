package com.example.matarpontun.domain.service

import com.example.matarpontun.domain.model.Ward
import com.example.matarpontun.domain.repository.WardRepository

class WardService(
    private val wardRepository: WardRepository
) {

    suspend fun login(
        wardName: String,
        password: String
    ): Result<Ward> {

        // Basic validation
        if (wardName.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Ward name cannot be empty")
            )
        }

        if (password.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Password cannot be empty")
            )
        }

        return try {
            val ward = wardRepository.login(wardName, password)

            if (ward != null) {
                Result.success(ward)
            } else {
                Result.failure(
                    IllegalArgumentException("Invalid credentials")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
