package com.github.pksokolowski.smogalert.api

import com.github.pksokolowski.smogalert.api.models.StationModel
import retrofit2.Call
import retrofit2.http.GET

interface StationsService {

    @GET("station/findAll")
    fun getStations(): Call<List<StationModel>>
}