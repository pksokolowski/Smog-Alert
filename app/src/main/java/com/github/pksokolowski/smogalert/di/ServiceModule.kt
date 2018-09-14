package com.github.pksokolowski.smogalert.di

import com.github.pksokolowski.smogalert.job.AirQualityCheckJobService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ServiceModule {
    @ContributesAndroidInjector()
    abstract fun contributeAirQualityCheckJobService(): AirQualityCheckJobService
}