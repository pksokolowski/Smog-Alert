package com.github.pksokolowski.smogalert.utils

import android.content.Context
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_AIR_QUALITY_MISSING
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_LOCATION_MISSING
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_NO_INTERNET
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_STATIONS_TOO_FAR_AWAY
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_NO_KNOWN_STATIONS
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_SUCCESS

/**
 * Provides an explanation of the error codes from AirQualityLog objects
 */
class ErrorExplanationHelper {
    companion object {
        fun explain(log: AirQualityLog, context: Context): String {
            if (log.airQualityIndex == -1 && log.errorCode == ERROR_CODE_SUCCESS) {
                val highestSubIndex = log.details.getHighestIndex()
                return if (highestSubIndex > -1) {
                    // partial data case
                    val possibleIndexTitle = AirQualityIndexHelper.getTitle(highestSubIndex, context)
                    context.getString(R.string.error_explanation_partial_data, possibleIndexTitle)
                }
                else context.getString(R.string.error_explanation_server)
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
    }
}