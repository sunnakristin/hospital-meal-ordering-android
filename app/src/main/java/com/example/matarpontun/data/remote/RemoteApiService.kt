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

/**
 * Typed Retrofit interface covering all backend endpoints used by the Android app.
 * All functions are suspend so they run on a coroutine and don't block the main thread.
 */
interface RemoteApiService {

    // --- Orders ---

    /** UC4 — Places a meal order for a single patient. Body contains the food type name. */
    @POST("patients/{id}/order")
    suspend fun createOrder(
        @Path("id") patientId: Long,
        @Body request: OrderRequest
    ): OrderResponseDto

    /** Places orders for every patient in the ward in one request. */
    @POST("wards/{id}/order")
    suspend fun orderWard(
        @Path("id") wardId: Long
    ): WardOrderResponseDto

    /** UC6 / US6 — Deletes the current day's order for the given patient. Returns 404 if none exists. */
    @DELETE("patients/{id}/order/today")
    suspend fun cancelTodaysOrder(
        @Path("id") patientId: Long
    ): Map<String, Any>

    // --- Patients ---

    /**
     * UC8 — Returns all patients for the ward identified by [request] credentials.
     * Note: uses POST with credentials in the body rather than a JWT header.
     */
    @POST("patients/all")
    suspend fun getWardPatients(
        @Body request: LoginRequest
    ): WardPatientsDto

    /** US7 — Updates a patient's name, food type, and restrictions. */
    @PATCH("patients/{id}")
    suspend fun updatePatient(
        @Path("id") patientId: Long,
        @Body request: PatientUpdateRequest
    ): Map<String, Any>

    // --- Wards ---

    /** US1 — Signs in a ward and returns a [Ward] with the assigned JWT token. */
    @POST("wards/signIn")
    suspend fun login(
        @Body request: LoginRequest
    ): Ward

    /** US13 — Creates a new ward account. */
    @POST("wards")
    suspend fun signUp(
        @Body request: LoginRequest
    ): Ward

    /** Returns summary information for the ward (name, room count, patient count). */
    @GET("wards/summary/{wardId}")
    suspend fun getWardSummary(
        @Path("wardId") wardId: Long
    ): Ward

    /** US5 — Updates the ward's name and password. */
    @PUT("wards/{id}")
    suspend fun updateWard(
        @Path("id") wardId: Long,
        @Body request: WardUpdateRequest
    ): Ward

    // --- Rooms ---

    /** Returns all rooms for the given ward, each including their patients. */
    @GET("wards/{wardId}/rooms")
    suspend fun getWardRooms(
        @Path("wardId") wardId: Long
    ): List<RoomCreatedResponse>

    /** Creates a new room with auto-generated patients for the given ward. */
    @POST("wards/{wardId}/rooms")
    suspend fun createRoom(
        @Path("wardId") wardId: Long,
        @Body request: RoomCreateRequest
    ): RoomCreatedResponse

    /** US11 — Looks up a room by its QR code string. Used after scanning. */
    @GET("wards/rooms/qr/{qrCode}")
    suspend fun getRoomByQrCode(
        @Path("qrCode") qrCode: String
    ): RoomLookupDto

    // --- Meals / Menu ---

    /** Returns the list of all available food types (e.g. Regular, Diabetic, Vegetarian). */
    @GET("meals/food-types")
    suspend fun getFoodTypes(): List<FoodTypeDto>

    /** Returns today's assigned menu for the given food type. Throws HTTP 404 if none is assigned. */
    @GET("meals/menu/{foodTypeId}")
    suspend fun getMenuForFoodType(@Path("foodTypeId") foodTypeId: Long): MenuDetailDto
}
