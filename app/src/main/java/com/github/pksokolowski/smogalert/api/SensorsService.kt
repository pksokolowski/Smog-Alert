package com.github.pksokolowski.smogalert.api

import com.github.pksokolowski.smogalert.api.models.SensorsModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SensorsService {

    @GET("station/sensors/{stationId}")
    fun getSensors(@Path("stationId") stationId: Long?): Call<List<SensorsModel>>
}