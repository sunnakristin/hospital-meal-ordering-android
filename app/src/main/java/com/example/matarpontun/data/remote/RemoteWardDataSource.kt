package com.example.matarpontun.data.remote

import com.example.matarpontun.domain.model.Ward

/**
 * Abstraction over the remote ward API for authentication and account management.
 * Implemented by [RemoteWardDataSourceImpl].
 */
interface RemoteWardDataSource {
    /** Signs in with the given credentials. Returns null if login fails. */
    suspend fun login(wardName: String, password: String): Ward?

    /** Registers a new ward account. Returns null if creation fails. */
    suspend fun createAccount(wardName: String, password: String): Ward?
}
