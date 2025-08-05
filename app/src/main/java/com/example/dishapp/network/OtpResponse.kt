package com.example.dishapp.network

data class OtpResponse(
    val success: Boolean,
    val message: String?,
    val data: Any?
)
