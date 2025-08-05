package com.example.dishapp.network

data class OtpRequest(
    val apikey: String = "sXhcPu0JneUbQ6TG2tXePK8MC2tBAHn9",
    val identifier: String,
    val language: String = "Vi",
    val method: String = "Sms",
    val otp_type: String = "Login"
)
