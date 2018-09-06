package com.github.pksokolowski.smogalert.repository

import com.github.pksokolowski.smogalert.database.AirQualityLogsDao
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

@PerApp
class AirQualityLogsRepository @Inject constructor(airQualityLogsDao: AirQualityLogsDao){
}