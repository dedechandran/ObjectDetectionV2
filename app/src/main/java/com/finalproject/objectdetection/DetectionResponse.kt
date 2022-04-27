package com.finalproject.objectdetection

import com.google.gson.annotations.SerializedName

data class DetectionResponse(
    @SerializedName("images") val detectedObject: List<Food>
){
    data class Food(
        @SerializedName("label") val foodLabel: String,
        @SerializedName("boundingBox") val foodBoundingbox: Coordinate,
        @SerializedName("confidence") val foodConfidence: Float,
        @SerializedName("color") val foodBoundingBoxColor: List<Int>
    ){
        data class Coordinate(
            @SerializedName("width") val foodWidth: Int,
            @SerializedName("height") val foodHeight: Int,
            @SerializedName("xAxis") val foodX: Int,
            @SerializedName("yAxis") val foodY: Int
        )
    }
}