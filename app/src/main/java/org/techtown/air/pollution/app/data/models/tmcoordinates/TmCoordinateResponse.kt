package org.techtown.air.pollution.app.data.models.tmcoordinates


import com.google.gson.annotations.SerializedName

data class TmCoordinateResponse(
    @SerializedName("documents")
    val documents: List<Document>?,
    @SerializedName("meta")
    val meta: Meta?
)