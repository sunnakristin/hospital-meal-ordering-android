package com.example.matarpontun.domain.repository

import com.example.matarpontun.domain.model.Ward

/**
 * Domain interface for ward authentication.
 * Implemented by [NetworkWardRepository] — no offline support for login/sign-up.
 */
interface WardRepository {
    /** Authenticates with [wardName] and [password]. Returns null if credentials are invalid. */
    suspend fun login(wardName: String, password: String): Ward?

    /** Creates a new ward account. Returns null if the name already exists or creation fails. */
    suspend fun createAccount(wardName: String, password: String): Ward?
}
