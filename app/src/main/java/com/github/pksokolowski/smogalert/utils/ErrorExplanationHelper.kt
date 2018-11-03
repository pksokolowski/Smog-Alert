package com.github.pksokolowski.smogalert.utils

import android.content.Context
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_AIR_QUALITY_MISSING
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_LOCATION_MISSING
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_NO_INTERNET
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_STATIONS_TOO_FAR_AWAY
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_NO_KNOWN_STATIONS
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_SUCCESS
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import java.util.*

/**
 * Provides an explanation of the error codes from AirQualityLog objects
 */
class ErrorExplanationHelper {
    companion object {
        fun explain(log: AirQualityLog, context: Context): String {
            if (log.airQualityIndex == -1 && log.errorCode == ERROR_CODE_SUCCESS) {
                val highestSubIndex = log.details.getHighestIndex()
                return if (highestSubIndex > -1) {
                    getPartialDataExplanation(highestSubIndex, log, context)
                } else {
                    context.getString(R.string.error_explanation_server)
                }
            }

            return when (log.errorCode) {
                ERROR_CODE_SUCCESS -> ""
                ERROR_CODE_NO_INTERNET -> context.getString(R.string.error_explanation_internet)
                ERROR_CODE_LOCATION_MISSING -> context.getString(R.string.error_explanation_location)
                ERROR_CODE_NO_KNOWN_STATIONS -> context.getString(R.string.error_explanation_stations_missing)
                ERROR_CODE_STATIONS_TOO_FAR_AWAY -> context.getString(R.string.error_explanation_stations_far_away)
                ERROR_CODE_AIR_QUALITY_MISSING -> context.getString(R.string.error_explanation_connection)
                else -> context.getString(R.string.error_explanation_unknown)
            }

        }

        private fun getPartialDataExplanation(highestSubIndex: Int, log: AirQualityLog, context: Context): String {
            val possibleIndexTitle = AirQualityIndexHelper.getTitle(highestSubIndex, context).toLowerCase()

            return if (areKeyPollutantsCovered(log))
                context.getString(R.string.error_explanation_partial_data, possibleIndexTitle)
            else
                context.getString(R.string.error_explanation_partial_data_without_key_pollutants, possibleIndexTitle)
        }

        private fun areKeyPollutantsCovered(log: AirQualityLog): Boolean {
            // establish whether or not the key pollutants are covered
            val gainedCoverage = log.details.getSensorCoverage()
            val expectedCoverage = log.expectedSensorCoverage

            // get month number, approximately
            val calendar = Calendar.getInstance(Locale.US).apply { timeInMillis = log.timeStamp }
            // month + 1, because Calendar's months start at 0, which is less intuitive dealing with months by number
            val month = calendar.get(Calendar.MONTH) + 1

            fun isExpectedButMissing(sensorFlag: Int) = expectedCoverage.hasSensors(sensorFlag) && !gainedCoverage.hasSensors(sensorFlag)

            // require O3 measurements, if should be available in the area, between March and October, inclusive on both ends
            if(month in 3..10 && isExpectedButMissing(FLAG_SENSOR_O3)) return false

            // require particulate matter readings during the colder part of the year
            if((month > 8 || month < 6) && isExpectedButMissing(FLAG_SENSOR_PM10) || isExpectedButMissing(FLAG_SENSOR_PM25)) return false

            return true
        }

    }
}