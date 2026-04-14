package com.example.matarpontun.data.remote

import com.example.matarpontun.data.remote.dto.FoodTypeDto
import com.example.matarpontun.data.remote.dto.LoginRequest
import com.example.matarpontun.data.remote.dto.MenuDetailDto
import com.example.matarpontun.data.remote.dto.OrderRequest
import com.example.matarpontun.data.remote.dto.OrderResponseDto
import com.example.matarpontun.data.remote.dto.PatientUpdateRequest
import com.example.matarpontun.data.remote.dto.RoomCreateRequest
import com.example.matarpontun.data.remote.dto.RoomCreatedResponse
import com.example.matarpontun.data.remote.dto.RoomLookupDto
import com.example.matarpontun.data.remote.dto.WardOrderResponseDto
import com.example.matarpontun.data.remote.dto.WardPatientsDto
import com.example.matarpontun.domain.model.DailyOrder
import com.example.matarpontun.domain.model.FoodType
import com.example.matarpontun.domain.model.Patient
import com.example.matarpontun.domain.model.Ward
import com.example.matarpontun.data.remote.dto.WardUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @POST("wards")
    suspend fun signUp(
        @Body request: LoginRequest
    ): Ward

    @POST("wards/{id}/order")
    suspend fun orderWard(
        @Path("id") wardId: Long
    ): WardOrderResponseDto

    @GET("wards/{wardId}/rooms")
    suspend fun getWardRooms(
        @Path("wardId") wardId: Long
    ): List<RoomCreatedResponse>

    @POST("wards/{wardId}/rooms")
    suspend fun createRoom(
        @Path("wardId") wardId: Long,
        @Body request: RoomCreateRequest
    ): RoomCreatedResponse

    @PATCH("patients/{id}")
    suspend fun updatePatient(
        @Path("id") patientId: Long,
        @Body request: PatientUpdateRequest
    ): Map<String, Any>

    @PUT("wards/{id}")
    suspend fun updateWard(
        @Path("id") wardId: Long,
        @Body request: WardUpdateRequest
    ): Ward

    /** Deletes the current day's order for the given patient (US6). Returns 404 if none exists. */
    @DELETE("patients/{id}/order/today")
    suspend fun cancelTodaysOrder(
        @Path("id") patientId: Long
    ): Map<String, Any>

    @GET("meals/food-types")
    suspend fun getFoodTypes(): List<FoodTypeDto>

    /** Returns the assigned menuOfTheDay for the given food type. Throws 404 if none is assigned. */
    @GET("meals/menu/{foodTypeId}")
    suspend fun getMenuForFoodType(@Path("foodTypeId") foodTypeId: Long): MenuDetailDto

    @GET("wards/rooms/qr/{qrCode}")
    suspend fun getRoomByQrCode(
        @Path("qrCode") qrCode: String
    ): RoomLookupDto
}
