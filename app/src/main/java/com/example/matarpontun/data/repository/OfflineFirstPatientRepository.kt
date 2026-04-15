package com.example.matarpontun.data.repository

import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.local.LocalPatientDataSourceImpl
import com.example.matarpontun.data.remote.RemotePatientDataSource
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository

/**
 * Offline-first implementation of [PatientRepository].
 *
 * On each fetch it first attempts a network call and saves the result to Room.
 * If the network is unavailable, it falls back to the cached patients for the current ward.
 * Sets [AppContainer.isOffline] so the UI can show an offline banner.
 */
class OfflineFirstPatientRepository(
    private val remoteDataSource: RemotePatientDataSource,
    private val localDataSource: LocalPatientDataSourceImpl,
    private val wardId: () -> Long   // lambda so it always reads the current value from AppContainer
) : PatientRepository {

    override suspend fun getPatientsByWard(wardId: Long): List<Patient> {
        val loginRequest = AppContainer.currentLoginRequest
            ?: throw IllegalStateException("No ward login information available")

        return try {
            // Network succeeded — save to cache and clear the offline flag
            val patients = remoteDataSource.getWardPatients(loginRequest)
            localDataSource.savePatients(this.wardId(), patients)
            AppContainer.isOffline = false
            patients
        } catch (e: Exception) {
            // Network failed — fall back to cached patients
            val cached = localDataSource.getPatientsByWard(this.wardId())
            if (cached.isNotEmpty()) {
                AppContainer.isOffline = true
                cached
            } else throw e  // No cache available, propagate the error
        }
    }
}
