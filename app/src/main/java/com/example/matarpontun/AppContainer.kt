package com.example.matarpontun

import com.example.matarpontun.data.network.RetrofitClient
import com.example.matarpontun.data.remote.RemoteApiService
import com.example.matarpontun.data.remote.RemoteDailyOrderDataSourceImpl
import com.example.matarpontun.data.remote.RemotePatientDataSourceImpl
import com.example.matarpontun.data.remote.RemoteWardDataSourceImpl
import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.data.repository.NetworkDailyOrderRepository
import com.example.matarpontun.data.repository.NetworkPatientRepository
import com.example.matarpontun.data.repository.NetworkWardRepository
import com.example.matarpontun.domain.repository.DailyOrderRepository
import com.example.matarpontun.domain.repository.PatientRepository
import com.example.matarpontun.domain.repository.WardRepository
import com.example.matarpontun.domain.service.DailyOrderService
import com.example.matarpontun.domain.service.PatientService
import com.example.matarpontun.domain.service.WardService

object AppContainer {

    private val retrofit = RetrofitClient.instance

    private val api: RemoteApiService =
        retrofit.create(RemoteApiService::class.java)

    // Holds the last successful ward login so we can call
    // UC8 endpoints that require WardDTO (LoginRequest) in the body.
    var currentLoginRequest: LoginRequest? = null

    // --- Daily Orders ---

    private val remoteDailyOrderDataSource =
        RemoteDailyOrderDataSourceImpl(api)

    val dailyOrderRepository: DailyOrderRepository =
        NetworkDailyOrderRepository(remoteDailyOrderDataSource)

    val dailyOrderService: DailyOrderService =
        DailyOrderService(dailyOrderRepository)

    // --- Patients ---

    private val remotePatientDataSource =
        RemotePatientDataSourceImpl(api)

    val patientRepository: PatientRepository =
        NetworkPatientRepository(remotePatientDataSource)

    val patientService =
        PatientService(patientRepository)

    // --- Ward ---
    private val remoteWardDataSource =
        RemoteWardDataSourceImpl(api)

    val wardRepository: WardRepository =
        NetworkWardRepository(remoteWardDataSource)

    val wardService =
        WardService(wardRepository)
}