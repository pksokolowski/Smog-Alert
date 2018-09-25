package com.github.pksokolowski.smogalert.utils

import com.github.pksokolowski.smogalert.airquality.models.AirQualityModel
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.database.PollutionDetails

class AirQualityLogDataConverter {
    companion object {
        fun toAirQualityLog(model: AirQualityModel, stationId: Long, timeStamp: Long): AirQualityLog {
            var sensors = 0

            val details = PollutionDetails(
                    model.pm10?.value ?: -1,
                    model.pm25?.value ?: -1,
                    model.o3?.value ?: -1,
                    model.no2?.value ?: -1,
                    model.so2?.value ?: -1,
                    model.c6h6?.value ?: -1,
                    model.co?.value ?: -1
            )

            val airQualityIndex = model.indexLevel?.value ?: -1

            return AirQualityLog(0,
                    airQualityIndex,
                    details,
                    stationId,
                    AirQualityLog.ERROR_CODE_SUCCESS,
                    timeStamp,
                    sensors)
        }
    }
}