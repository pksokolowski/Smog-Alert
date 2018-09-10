package com.github.pksokolowski.smogalert.airquality

import com.github.pksokolowski.smogalert.airquality.models.StationModel
import retrofit2.Call
import retrofit2.http.GET

interface StationsService {

    @get:GET("station/findAll")
    val getStations: Call<List<StationModel>>
}