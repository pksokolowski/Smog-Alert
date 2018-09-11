package com.github.pksokolowski.smogalert.airquality.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class AirQualityModel {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("stIndexLevel")
    @Expose
    var indexLevel: IndexLevel? = null

    class IndexLevel {
        @SerializedName("id")
        @Expose
        var value: Int? = null
    }
}