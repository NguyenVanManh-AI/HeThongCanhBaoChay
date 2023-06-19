package com.example.fire_warning_app

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET

interface SensorApi {
    @GET("sensor_data/get")
    fun getSensorData(): Call<SensorData>
    data class SensorData(
        @SerializedName("firedetected_value") val fireDetectedValue: Boolean,
        @SerializedName("smoke_value") val smokeValue: Float,
        @SerializedName("temperature_value") val temperatureValue: Float
    )
}
