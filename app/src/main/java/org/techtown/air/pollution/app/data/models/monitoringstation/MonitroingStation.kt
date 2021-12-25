package org.techtown.air.pollution.app.data.models.monitoringstation


import com.google.gson.annotations.SerializedName

data class MonitroingStation(
    @SerializedName("addr")
    val addr: String?,
    @SerializedName("stationName")
    val stationName: String?,
    @SerializedName("tm")
    val tm: Double?
)