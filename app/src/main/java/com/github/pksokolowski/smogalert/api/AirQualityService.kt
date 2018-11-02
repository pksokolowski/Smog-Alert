package com.github.pksokolowski.smogalert.api

import com.github.pksokolowski.smogalert.api.models.AirQualityModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface AirQualityService {

    @GET("aqindex/getIndex/{stationId}")
    fun getCurrentAQ(@Path("stationId") stationId: Long?): Call<AirQualityModel>
}
