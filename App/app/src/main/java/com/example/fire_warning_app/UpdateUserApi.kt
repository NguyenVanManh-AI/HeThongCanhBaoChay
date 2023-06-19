package com.example.fire_warning_app

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

import retrofit2.http.PUT


interface UpdateUserApi {
    @PUT("update")
    fun updateUser(@Body user: UserUpdate): Call<Void>
}
data class UserUpdate(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String
)