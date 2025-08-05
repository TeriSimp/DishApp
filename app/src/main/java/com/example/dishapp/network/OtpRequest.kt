package com.example.dishapp.network

data class OtpRequest(
    val apikey: String = "kUCqqbfEQxkZge7HHDFcIxfoHzqSZUam",
    val identifier: String,
    val language: String = "Vi",
    val method: String = "Sms",
    val otp_type: String = "Login"
)
