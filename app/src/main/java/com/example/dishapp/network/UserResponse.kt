package com.example.dishapp.network

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("name") val name: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("about_me") val aboutMe: String?,
    @SerializedName("project_id") val projectId: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?
)
