package com.github.pksokolowski.smogalert.airquality.models


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StationModel {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("gegrLat")
    @Expose
    var gegrLat: String? = null
    @SerializedName("gegrLon")
    @Expose
    var gegrLon: String? = null

}