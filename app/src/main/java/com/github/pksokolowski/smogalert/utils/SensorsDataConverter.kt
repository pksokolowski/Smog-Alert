package com.github.pksokolowski.smogalert.utils

import com.github.pksokolowski.smogalert.api.models.SensorsModel
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_CO
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_NO2
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_SO2

class SensorsDataConverter {
    companion object {
        fun toSensorFlags(sensors: List<SensorsModel>): Int {
            var sensorFlags = 0

            for (s in sensors) {
                val paramCode = s.param?.paramCode ?: continue
                val matchingFlag = SENSOR_CODE_TO_SENSOR_FLAG[paramCode] ?: continue
                sensorFlags = sensorFlags or matchingFlag
            }

            return sensorFlags
        }

        val SENSOR_CODE_TO_SENSOR_FLAG = mapOf(
                "PM10" to FLAG_SENSOR_PM10,
                "PM2.5" to FLAG_SENSOR_PM25,
                "O3" to FLAG_SENSOR_O3,
                "NO2" to FLAG_SENSOR_NO2,
                "SO2" to FLAG_SENSOR_SO2,
                "C6H6" to FLAG_SENSOR_C6H6,
                "CO" to FLAG_SENSOR_CO
        )
    }
}