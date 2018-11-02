package com.github.pksokolowski.smogalert.api.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class AirQualityModel {

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("stIndexLevel")
    @Expose
    var indexLevel: IndexLevel? = null

    @SerializedName("so2IndexLevel")
    @Expose
    var so2: IndexLevel? = null

    @SerializedName("no2IndexLevel")
    @Expose
    var no2: IndexLevel? = null

    @SerializedName("coIndexLevel")
    @Expose
    var co: IndexLevel? = null

    @SerializedName("pm10IndexLevel")
    @Expose
    var pm10: IndexLevel? = null

    @SerializedName("pm25IndexLevel")
    @Expose
    var pm25: IndexLevel? = null

    @SerializedName("o3IndexLevel")
    @Expose
    var o3: IndexLevel? = null

    @SerializedName("c6h6IndexLevel")
    @Expose
    var c6h6: IndexLevel? = null

    class IndexLevel {
        @SerializedName("id")
        @Expose
        var value: Int? = null
    }
}