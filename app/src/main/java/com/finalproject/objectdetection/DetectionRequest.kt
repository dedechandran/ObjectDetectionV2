package com.finalproject.objectdetection

import com.google.gson.annotations.SerializedName

data class DetectionRequest(
    @SerializedName("encoded_image") val encodedImage: String
)