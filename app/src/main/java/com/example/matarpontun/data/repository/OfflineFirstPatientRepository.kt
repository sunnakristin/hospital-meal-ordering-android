package com.example.matarpontun.data.repository

import com.example.matarpontun.AppContainer
import com.example.matarpontun.data.local.LocalPatientDataSourceImpl
import com.example.matarpontun.data.remote.RemotePatientDataSource
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.repository.PatientRepository

class OfflineFirstPatientRepository(
    private val remoteDataSource: RemotePatientDataSource,
    private val localDataSource: LocalPatientDataSourceImpl,
    private val wardId: () -> Long
) : PatientRepository {

    override suspend fun getPatientsByWard(wardId: Long): List<Patient> {
        val loginRequest = AppContainer.currentLoginRequest
            ?: throw IllegalStateException("No ward login information available")

        return try {
            val patients = remoteDataSource.getWardPatients(loginRequest)
            localDataSource.savePatients(this.wardId(), patients)
            AppContainer.isOffline = false
            patients
        } catch (e: Exception) {
            val cached = localDataSource.getPatientsByWard(this.wardId())
            if (cached.isNotEmpty()) {
                AppContainer.isOffline = true
                cached
            } else throw e
        }
    }
}
