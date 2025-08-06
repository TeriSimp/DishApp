package com.example.dishapp.network

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(
            CurlInterceptor(
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.i("[RetrofitClient]", ":CURL $message")
                    }
                }
            )
        )
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api-trieve.ermis.network/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}