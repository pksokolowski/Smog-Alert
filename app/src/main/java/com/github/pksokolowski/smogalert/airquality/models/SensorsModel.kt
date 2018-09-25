package com.github.pksokolowski.smogalert.airquality.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class SensorsModel {

    @SerializedName("id")
    @Expose
    var id: Int? = null
    @SerializedName("stationId")
    @Expose
    var stationId: Int? = null
    @SerializedName("param")
    @Expose
    var param: Param? = null


    class Param {

        @SerializedName("paramName")
        @Expose
        var paramName: String? = null
        @SerializedName("paramFormula")
        @Expose
        var paramFormula: String? = null
        @SerializedName("paramCode")
        @Expose
        var paramCode: String? = null
        @SerializedName("idParam")
        @Expose
        var idParam: Int? = null

    }
}