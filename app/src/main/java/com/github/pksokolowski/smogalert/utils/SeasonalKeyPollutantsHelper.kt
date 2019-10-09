package com.github.pksokolowski.smogalert.utils

import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import java.util.*
import javax.inject.Inject

@PerApp
class SeasonalKeyPollutantsHelper @Inject constructor() {

    /**
     * Checks if the log covers the key pollutants that it has in it's expectedCoverage
     */
    fun coversKeyPollutantsIfExpected(log: AirQualityLog) = coversKeyPollutantsIfExpected(
            log.details.getSensorCoverage(),
            log.expectedSensorCoverage,
            log.timeStamp)

    /**
     * Checks if the log covers the key pollutants that it has in it's expectedCoverage
     */
    fun coversKeyPollutantsIfExpected(gainedCoverage: SensorsPresence, expectedCoverage: SensorsPresence, timeStamp: Long): Boolean {
        val month = getApproxMonthNumber(timeStamp)

        fun isExpectedButMissing(sensorFlag: Int) = expectedCoverage.hasSensors(sensorFlag) && !gainedCoverage.hasSensors(sensorFlag)

        if (isOzoneSeason(month) && isExpectedButMissing(FLAG_SENSOR_O3)) return false
        if (isPMSeason(month) && isExpectedButMissing(FLAG_SENSOR_PM10) || isExpectedButMissing(FLAG_SENSOR_PM25)) return false

        return true
    }

    /**
     * Combines provided sensors coverage with key sensors at the time. If any of PM sensors are
     * present, it's considered to meet minimum requirements and doesn't trigger the combination.
     * When both are missing, both are added though.
     */
    fun includeKeyPollutants(sensors: SensorsPresence, timeStamp: Long): SensorsPresence {
        val month = getApproxMonthNumber(timeStamp)
        var correctedCoverage = SensorsPresence(sensors.sensorFlags)

        if (isOzoneSeason(month) && !sensors.hasSensors(FLAG_SENSOR_O3)) {
            correctedCoverage = correctedCoverage.combinedWith(FLAG_SENSOR_O3)
        }
        if (isPMSeason(month) && (!sensors.hasSensors(FLAG_SENSOR_PM10) && !sensors.hasSensors(FLAG_SENSOR_PM25))) {
            correctedCoverage = correctedCoverage.combinedWith(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25)
        }

        return correctedCoverage
    }

    private fun getApproxMonthNumber(timeStamp: Long): Int {
        val calendar = Calendar.getInstance(Locale.US).apply { timeInMillis = timeStamp }
        // month + 1, because Calendar's months start at 0, which is less intuitive.
        return calendar.get(Calendar.MONTH) + 1
    }

    private fun isOzoneSeason(monthNumber: Int) = monthNumber in 4..9
    private fun isPMSeason(monthNumber: Int) = (monthNumber > 8 || monthNumber < 6)
}