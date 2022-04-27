package com.finalproject.objectdetection

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("detect")
    suspend fun detectObject(@Body request: DetectionRequest): DetectionResponse
}