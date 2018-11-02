package com.github.pksokolowski.smogalert.api.models


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StationModel {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("gegrLat")
    @Expose
    var latitude: String? = null
    @SerializedName("gegrLon")
    @Expose
    var longitude: String? = null

}