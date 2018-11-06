package com.github.pksokolowski.smogalert.utils

import com.github.pksokolowski.smogalert.api.models.AirQualityModel
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_AIR_QUALITY_MISSING
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_SUCCESS
import com.github.pksokolowski.smogalert.db.PollutionDetails

class AirQualityLogDataConverter {
    companion object {
        fun toAirQualityLog(model: AirQualityModel, stationId: Long, timeStamp: Long): AirQualityLog {
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

            // "id" and "status" fields are always set, unless station doesn't exist or Retrofit didn't
            // obtain any answer, like when connection is tearing, wifi failing or a bit out of range
            val errorCode = if (model.id == null && model.status == null) ERROR_CODE_AIR_QUALITY_MISSING
            else ERROR_CODE_SUCCESS

            return AirQualityLog(0,
                    airQualityIndex,
                    details,
                    stationId,
                    errorCode,
                    timeStamp,
                    0)
        }
    }
}