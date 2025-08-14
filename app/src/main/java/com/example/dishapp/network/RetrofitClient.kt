package com.example.dishapp.network

import android.content.Context
import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var retrofit: Retrofit? = null

    private fun getRetrofit(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context).also { retrofit = it }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val curl = CurlInterceptor(
            logger = object : Logger {
                override fun log(message: String) {
                    Log.i("[RetrofitClient]", ":CURL $message")
                }
            }
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(logging)
            .addInterceptor(curl)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api-trieve.ermis.network/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun authService(context: Context): AuthService =
        getRetrofit(context.applicationContext).create(AuthService::class.java)
}
