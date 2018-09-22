package com.github.pksokolowski.smogalert.utils

import com.github.pksokolowski.smogalert.airquality.models.AirQualityModel
import com.github.pksokolowski.smogalert.database.AirQualityLog

class AirQualityLogDataConverter {
    companion object {
        fun toAirQualityLog(model: AirQualityModel, stationId: Long, timeStamp: Long): AirQualityLog {
            var sensors = 0

            val pairs = listOf(
                    model.pm10 to AirQualityLog.FLAG_SENSOR_PM10,
                    model.pm25 to AirQualityLog.FLAG_SENSOR_PM25,
                    model.o3 to AirQualityLog.FLAG_SENSOR_O3,
                    model.no2 to AirQualityLog.FLAG_SENSOR_NO2,
                    model.so2 to AirQualityLog.FLAG_SENSOR_SO2,
                    model.c6h6 to AirQualityLog.FLAG_SENSOR_C6H6,
                    model.co to AirQualityLog.FLAG_SENSOR_CO
            )

            for (p in pairs) {
                if (p.first == null) continue
                sensors = sensors or p.second
            }

            val airQualityIndex = model.indexLevel?.value ?: -1

            return AirQualityLog(0,
                    airQualityIndex,
                    stationId,
                    AirQualityLog.ERROR_CODE_SUCCESS,
                    timeStamp,
                    sensors)
        }
    }
}