package com.example.dishapp.network

data class VerifyResponse(
    val token: String,
    val refresh_token: String?,
    val user_id: String?,
    val project_id: String?,
    val phone: String?
)