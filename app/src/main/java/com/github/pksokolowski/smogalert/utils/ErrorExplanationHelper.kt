package com.github.pksokolowski.smogalert.utils

import android.app.Application
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_AIR_QUALITY_MISSING
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_LOCATION_MISSING
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_NO_INTERNET
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_NO_KNOWN_STATIONS
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.ERROR_CODE_STATIONS_TOO_FAR_AWAY
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

/**
 * Provides an explanation of the error codes from AirQualityLog objects
 */
@PerApp
class ErrorExplanationHelper @Inject constructor(private val context: Application) {
    fun explain(log: AirQualityLog): String {
        // handle errors if any
        if (log.errorCode > 0) return when (log.errorCode) {
            ERROR_CODE_NO_INTERNET -> context.getString(R.string.error_explanation_internet)
            ERROR_CODE_LOCATION_MISSING -> context.getString(R.string.error_explanation_location)
            ERROR_CODE_NO_KNOWN_STATIONS -> context.getString(R.string.error_explanation_stations_missing)
            ERROR_CODE_STATIONS_TOO_FAR_AWAY -> context.getString(R.string.error_explanation_stations_far_away)
            ERROR_CODE_AIR_QUALITY_MISSING -> context.getString(R.string.error_explanation_connection)
            else -> context.getString(R.string.error_explanation_unknown)
        }

        if (log.hasIndex()) {
            return if (log.hasExpectedCoverage()) "" else context.getString(R.string.error_explanation_partial_data)
        }

        val highestSubIndex = log.details.getHighestIndex()
        return if (highestSubIndex > -1) {
            val possibleIndexTitle = AirQualityIndexHelper.getTitle(highestSubIndex, context).toLowerCase()
            context.getString(R.string.error_explanation_partial_data_without_key_pollutants, possibleIndexTitle)
        } else {
            context.getString(R.string.error_explanation_server)
        }

    }
}