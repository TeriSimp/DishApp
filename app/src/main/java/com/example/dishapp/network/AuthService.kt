package com.example.dishapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {
    @POST("uss/v1/auth/get_otp_new")
    suspend fun getOtp(
        @Query("api_key") apiKey: String = "sXhcPu0JneUbQ6TG2tXePK8MC2tBAHn9",
        @Query("user_id") userId: String = "0x5c404115fab8ea5ab9fc1eff1c5575968330e384",
        @Body body: OtpRequest
    ): Response<OtpResponse>

    @POST("uss/v1/auth/otp_login")
    suspend fun verifyOtp(
        @Query("api_key") apiKey: String = "sXhcPu0JneUbQ6TG2tXePK8MC2tBAHn9",
        @Query("user_id") userId: String = "0x5c404115fab8ea5ab9fc1eff1c5575968330e384",
        @Body body: VerifyRequest
    ): Response<VerifyResponse>

    @GET("uss/v1/users/{user_id}")
    suspend fun getUserInfo(
        @Header("Authorization") authorization: String,
        @Path("user_id") userId: String,
        @Query("project_id") projectId: String,
        @Query("api_key") apiKey: String = "sXhcPu0JneUbQ6TG2tXePK8MC2tBAHn9",
    ): Response<UserResponse>
}
