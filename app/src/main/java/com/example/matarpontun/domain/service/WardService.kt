package com.example.matarpontun.domain.service

import com.example.matarpontun.domain.model.Ward
import com.example.matarpontun.domain.repository.WardRepository

/**
 * Domain service for ward authentication and account management.
 * Validates inputs before delegating to [WardRepository], returning
 * [Result.failure] with a user-readable message on validation or network errors.
 */
class WardService(
    private val wardRepository: WardRepository
) {

    /**
     * Validates credentials and signs in the ward.
     * Returns [Result.failure] with a message if either field is blank or credentials are invalid.
     */
    suspend fun login(
        wardName: String,
        password: String
    ): Result<Ward> {

        if (wardName.isBlank()) {
            return Result.failure(IllegalArgumentException("Ward name cannot be empty"))
        }

        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }

        return try {
            val ward = wardRepository.login(wardName, password)
            if (ward != null) Result.success(ward)
            else Result.failure(IllegalArgumentException("Invalid credentials"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates credentials and creates a new ward account.
     * Returns [Result.failure] if either field is blank or the ward name is already taken.
     */
    suspend fun createAccount(
        wardName: String,
        password: String
    ): Result<Ward> {
        if (wardName.isBlank()) {
            return Result.failure(IllegalArgumentException("Ward name cannot be empty"))
        }

        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }

        return try {
            val ward = wardRepository.createAccount(wardName, password)
            if (ward != null) Result.success(ward)
            else Result.failure(IllegalArgumentException("Ward name already exists or creation failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
