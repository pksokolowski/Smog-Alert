package com.github.pksokolowski.smogalert.airquality

import com.github.pksokolowski.smogalert.airquality.models.StationModel
import retrofit2.Call
import retrofit2.http.GET

interface StationsService {

    @GET("station/findAll")
    fun getStations(): Call<List<StationModel>>
}