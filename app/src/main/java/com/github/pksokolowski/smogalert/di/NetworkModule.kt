package com.github.pksokolowski.smogalert.di

import com.github.pksokolowski.smogalert.airquality.AirQualityService
import com.github.pksokolowski.smogalert.airquality.SensorsService
import com.github.pksokolowski.smogalert.airquality.StationsService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
open class NetworkModule {

    @PerApp
    @Provides
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
            .baseUrl("https://api.gios.gov.pl/pjp-api/rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @PerApp
    @Provides
    fun provideAirQualityService(retrofit: Retrofit): AirQualityService =
            retrofit.create(AirQualityService::class.java)

    @PerApp
    @Provides
    fun provideStationsService(retrofit: Retrofit): StationsService =
            retrofit.create(StationsService::class.java)

    @PerApp
    @Provides
    fun providesSensorsService(retrofit: Retrofit): SensorsService =
            retrofit.create(SensorsService::class.java)
}
