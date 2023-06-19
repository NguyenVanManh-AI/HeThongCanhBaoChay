package com.example.fire_warning_app

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.POST

interface LogoutApi {
    @POST("/logout")
    fun logout(): Call<LogoutResponse>
}

data class LogoutResponse(
    @SerializedName("message") val message: String
)
