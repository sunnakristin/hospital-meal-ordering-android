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

/**
 * Manual dependency injection container. Acts as the single source of truth for all
 * shared dependencies (Retrofit, Room, repositories, services) and current session state.
 *
 * Must be initialised via [init] in [MatarpontunApp.onCreate] before any Activity starts.
 * Local data sources are lateinit because Room requires a [Context] to build the database.
 */
object AppContainer {

    private val retrofit = RetrofitClient.instance

    /** Typed Retrofit interface used to make all API calls. */
    val api: RemoteApiService =
        retrofit.create(RemoteApiService::class.java)

    /**
     * The credentials from the last successful login.
     * Stored here because the backend's UC8 endpoint (GET patients) requires
     * ward credentials in the request body rather than a JWT header.
     */
    var currentLoginRequest: LoginRequest? = null

    /** Ward ID of the currently signed-in ward. Used by offline repositories to scope cached data. */
    var currentWardId: Long = -1L

    /** True when the last patient/order fetch fell back to the local Room cache. Drives the offline banner. */
    var isOffline: Boolean = false

    // --- Session (DataStore) ---

    /** Persists ward login across app restarts. Initialised in [init]. */
    lateinit var wardSessionDataStore: WardSessionDataStore
        private set

    // --- Daily Orders ---

    private val remoteDailyOrderDataSource =
        RemoteDailyOrderDataSourceImpl(api)

    private lateinit var localDailyOrderDataSource: LocalDailyOrderDataSourceImpl
    private lateinit var localPatientDataSource: LocalPatientDataSourceImpl

    /**
     * Tries the network first, falls back to the Room cache on failure.
     * Uses a lambda for wardId so it always reads the current [currentWardId].
     */
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

    /**
     * Tries the network first, falls back to the Room cache on failure.
     * Uses a lambda for wardId so it always reads the current [currentWardId].
     */
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

    /** Ward repository is network-only — there is no offline fallback for login/account creation. */
    val wardRepository: WardRepository =
        NetworkWardRepository(remoteWardDataSource)

    val wardService =
        WardService(wardRepository)

    /**
     * Initialises all context-dependent dependencies. Must be called once in
     * [MatarpontunApp.onCreate] before any Activity starts.
     */
    fun init(context: Context) {
        val db = AppDatabase.getInstance(context)
        localDailyOrderDataSource = LocalDailyOrderDataSourceImpl(db.dailyOrderDao())
        localPatientDataSource = LocalPatientDataSourceImpl(db.patientDao())
        wardSessionDataStore = WardSessionDataStore(context)
    }
}
