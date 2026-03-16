package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.data.remote.dto.OrderRequest
import com.example.matarpontun.data.remote.dto.OrderResponseDto
import com.example.matarpontun.data.remote.dto.WardPatientsDto
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.model.Ward
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteApiService {
        @POST("patients/{id}/order")
    suspend fun createOrder(
        @Path("id") patientId: Long,
        @Body request: OrderRequest
    ): OrderResponseDto

    @POST("patients/all")
    suspend fun getWardPatients(
        @Body request: LoginRequest
    ): WardPatientsDto

    @GET("wards/summary/{wardId}")
    suspend fun getWardSummary(
        @Path("wardId") wardId: Long
    ): Ward

    @POST("wards/signIn")
    suspend fun login(
        @Body request: LoginRequest
    ): Ward

    @POST("wards/{id}/order")
    suspend fun orderWard(
        @Path("id") wardId: Long
    ): Map<String, Any>
}
