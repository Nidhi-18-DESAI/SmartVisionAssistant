package com.example.smartvisionassistant.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class Prediction(
    val label: String,
    val score: Double
)

data class BackendResponse(
    val status: String,
    val message: String,
    val processed_image: String?,
    val prediction: Prediction?,
    val description: String?
)

interface ApiService {

    @GET("/")
    suspend fun checkBackend(): BackendResponse

    @Multipart
    @POST("/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<BackendResponse>
}