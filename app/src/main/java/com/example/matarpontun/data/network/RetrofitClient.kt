package com.example.matarpontun.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit instance shared across the app.
 * To point the app at a local emulator instead, swap the BASE_URL constant.
 */
object RetrofitClient {

    // private const val BASE_URL = "http://10.0.2.2:8080/" // local emulator (host machine localhost)
    private const val BASE_URL = "https://matarpontun.onrender.com/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
