package com.example.dishapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api-trieve.ermis.network/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}
