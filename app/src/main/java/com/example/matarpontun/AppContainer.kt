package com.example.matarpontun

import android.content.Context
import com.example.matarpontun.data.local.AppDatabase
import com.example.matarpontun.data.local.LocalDailyOrderDataSourceImpl
import com.example.matarpontun.data.local.LocalPatientDataSourceImpl
import com.example.matarpontun.data.local.WardSessionDataStore
import com.example.matarpontun.data.network.RetrofitClient
import com.example.matarpontun.data.remote.RemoteApiService
import com.example.matarpontun.data.remote.RemoteDailyOrderDataSourceImpl
import com.example.matarpontun.data.remote.RemotePatientDataSourceImpl
import com.example.matarpontun.data.remote.RemoteWardDataSourceImpl
import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.data.repository.OfflineFirstPatientRepository
import com.example.matarpontun.data.repository.NetworkWardRepository
import com.example.matarpontun.data.repository.OfflineFirstDailyOrderRepository
import com.example.matarpontun.domain.repository.DailyOrderRepository
import com.example.matarpontun.domain.repository.PatientRepository
import com.example.matarpontun.domain.repository.WardRepository
import com.example.matarpontun.domain.service.DailyOrderService
import com.example.matarpontun.domain.service.PatientService
import com.example.matarpontun.domain.service.WardService

object AppContainer {

    private val retrofit = RetrofitClient.instance

    val api: RemoteApiService =
        retrofit.create(RemoteApiService::class.java)

    // Holds the last successful ward login so we can call
    // UC8 endpoints that require WardDTO (LoginRequest) in the body.
    var currentLoginRequest: LoginRequest? = null
    var currentWardId: Long = -1L
    var isOffline: Boolean = false

    // --- Session (DataStore) ---
    lateinit var wardSessionDataStore: WardSessionDataStore
        private set

    // --- Daily Orders ---

    private val remoteDailyOrderDataSource =
        RemoteDailyOrderDataSourceImpl(api)

    private lateinit var localDailyOrderDataSource: LocalDailyOrderDataSourceImpl
    private lateinit var localPatientDataSource: LocalPatientDataSourceImpl

    val dailyOrderRepository: DailyOrderRepository by lazy {
        OfflineFirstDailyOrderRepository(
            remoteDataSource = remoteDailyOrderDataSource,
            localDataSource = localDailyOrderDataSource,
            wardId = { currentWardId }
        )
    }

    val dailyOrderService: DailyOrderService by lazy {
        DailyOrderService(dailyOrderRepository)
    }

    // --- Patients ---

    private val remotePatientDataSource =
        RemotePatientDataSourceImpl(api)

    val patientRepository: PatientRepository by lazy {
        OfflineFirstPatientRepository(
            remoteDataSource = remotePatientDataSource,
            localDataSource = localPatientDataSource,
            wardId = { currentWardId }
        )
    }

    val patientService: PatientService by lazy {
        PatientService(patientRepository)
    }

    // --- Ward ---

    private val remoteWardDataSource =
        RemoteWardDataSourceImpl(api)

    val wardRepository: WardRepository =
        NetworkWardRepository(remoteWardDataSource)

    val wardService =
        WardService(wardRepository)

    /**
     * Must be called once in MatarpontunApp.onCreate() before any Activity starts.
     */
    fun init(context: Context) {
        val db = AppDatabase.getInstance(context)
        localDailyOrderDataSource = LocalDailyOrderDataSourceImpl(db.dailyOrderDao())
        localPatientDataSource = LocalPatientDataSourceImpl(db.patientDao())
        wardSessionDataStore = WardSessionDataStore(context)
    }
}
