package org.techtown.air.pollution.app.data.models.monitoringstation


import com.google.gson.annotations.SerializedName

data class Body(
    @SerializedName("items")
    val monitroingStations: List<MonitroingStation>?,
    @SerializedName("numOfRows")
    val numOfRows: Int?,
    @SerializedName("pageNo")
    val pageNo: Int?,
    @SerializedName("totalCount")
    val totalCount: Int?
)