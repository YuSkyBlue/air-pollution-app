package org.techtown.air.pollution.app.data.models.airquality


import com.google.gson.annotations.SerializedName

data class AirQualityResonse(
    @SerializedName("response")
    val response: Response?
)