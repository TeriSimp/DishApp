package com.example.dishapp.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import com.example.dishapp.data.TokenStore

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenStore.getToken(context)
        val reqBuilder = chain.request().newBuilder()
            .header("Content-Type", "application/json;charset=utf-8")
            .header("Accept", "application/json")
            .header("Cache-Control", "no-cache")

        token?.let { reqBuilder.header("Authorization", "Bearer $it") }

        return chain.proceed(reqBuilder.build())
    }
}
