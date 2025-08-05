package com.example.dishapp.network

data class VerifyRequest(
    val identifier: String,
    val apikey: String,
    val otp: String,
    val method: String
)