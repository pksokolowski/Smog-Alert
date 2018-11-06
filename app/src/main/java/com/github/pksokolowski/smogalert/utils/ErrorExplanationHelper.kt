package com.github.pksokolowski.smogalert.utils

import android.content.Context
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_AIR_QUALITY_MISSING
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_LOCATION_MISSING
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_NO_INTERNET
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_NO_KNOWN_STATIONS
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_STATIONS_TOO_FAR_AWAY
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_SUCCESS
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

/**
 * Provides an explanation of the error codes from AirQualityLog objects
 */
@PerApp
class ErrorExplanationHelper @Inject constructor(private val seasonalKeyPollutantsHelper: SeasonalKeyPollutantsHelper) {
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

            return if (seasonalKeyPollutantsHelper.coversKeyPollutantsIfExpected(log))
                context.getString(R.string.error_explanation_partial_data, possibleIndexTitle)
            else
                context.getString(R.string.error_explanation_partial_data_without_key_pollutants, possibleIndexTitle)
        }
}