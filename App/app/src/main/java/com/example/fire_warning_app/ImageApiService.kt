package com.example.fire_warning_app

import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApiService {

    @GET("/image/all")
    suspend fun getAllImages(): List<ImageData>

    @GET("/image/all")
    suspend fun getImagesPerPage(
        @Query("start") start: Int,
        @Query("perPage") perPage: Int
    ): List<ImageData>
}

data class ImageData(
    val id: Int,
    val image_url: String,
    val created_at: String
)
