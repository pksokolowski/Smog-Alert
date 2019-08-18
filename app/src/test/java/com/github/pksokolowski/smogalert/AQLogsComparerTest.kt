package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.job.AQLogsComparer
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_CO
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_SO2
import org.junit.Assert
import org.junit.Test

class AQLogsComparerTest {
    @Test
    fun ignoresNoChangeCaseWithGoodAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 0, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 0, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresNoChangeCaseWithBadAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 5, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresDegradationWhenNotCrossingThreshold() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresDegradationWhenNotCrossingThresholdAndWithGoodAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 2, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresImprovementWhenNotCrossingThresholdAndWithGoodAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 3, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 1, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsDegradation() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 3, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 4, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_DEGRADED_PAST_THRESHOLD, result)
    }

    @Test
    fun detectsImprovementPastThreshold() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_IMPROVED_PAST_THRESHOLD, result)
    }


    @Test
    fun ignoresRepeatingError() {
        // location is missing twice in a row. Only the first instance of such error is to be noticed.
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsNewErrors() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_ERROR_EMERGED, result)
    }

    @Test
    fun detectsNewDataShortage() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 0, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = -1, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_DATA_SHORTAGE_STARTED, result)
    }

    @Test
    fun ignoresRepetitiveDataShortage() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = -1, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsDataShortageEndWhenAirIsOk() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1, details = PollutionDetails(9999999), expectedSensorCoverage = SensorsPresence(127))
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2, details = PollutionDetails(1111111), expectedSensorCoverage = SensorsPresence(127))

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_OK_AFTER_SHORTAGE_ENDED, result)
    }

    @Test
    fun detectsPartialDataShortageEndWhenAirIsOk() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1, details = PollutionDetails(9995599), expectedSensorCoverage = SensorsPresence(127))
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2, details = PollutionDetails(1111111), expectedSensorCoverage = SensorsPresence(127))

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_OK_AFTER_SHORTAGE_ENDED, result)
    }

    @Test
    fun detectsPartialDataShortageEndWhenAirIsBadAndWasPartOk() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1, details = PollutionDetails(9990099), expectedSensorCoverage = SensorsPresence(127))
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2, details = PollutionDetails(1115511), expectedSensorCoverage = SensorsPresence(127))

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_BAD_AFTER_SHORTAGE_ENDED, result)
    }

    @Test
    fun detectsDegradationWhenAirIsBadAndWasLikelyOk() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 1, timeStamp = 1, details = PollutionDetails(1000099), expectedSensorCoverage = SensorsPresence(127))
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2, details = PollutionDetails(1115511), expectedSensorCoverage = SensorsPresence(127))

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_DEGRADED_PAST_THRESHOLD, result)
    }

    @Test
    fun detectsBadAirRightAfterDataShortage() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1, details = PollutionDetails(9999999), expectedSensorCoverage = SensorsPresence(127))
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2, details = PollutionDetails(5555555), expectedSensorCoverage = SensorsPresence(127))

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_BAD_AFTER_SHORTAGE_ENDED, result)
    }

    @Test
    fun ignoresGoodAirWhenPreviousLogIsNull() {
        // no change case, good index
        val previous = null
        val current = AirQualityLog(id = 2, airQualityIndex = 0, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsNewErrorWhenPreviousLogIsNull() {
        // no change case, good index
        val previous = null
        val current = AirQualityLog(id = 2, airQualityIndex = 0, timeStamp = 2, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_ERROR_EMERGED, result)
    }

    @Test
    fun ignoresCaseWhereBothLogsAreNull() {
        // no change case, both null
        val previous = null
        val current = null

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresTransitionFromPartBadToLikelyBad() {
        val sensors = FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO

        val previous = AirQualityLog(1,
                -1,
                PollutionDetails(9999933),
                400,
                0,
                1,
                1,
                SensorsPresence(sensors))

        val current = AirQualityLog(2,
                3,
                PollutionDetails(3339933),
                400,
                0,
                2,
                1,
                SensorsPresence(sensors))

        val result = AQLogsComparer.compare(current, previous, 3)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsShortageWhenStartsWithCurentNotHavingKeyPollutants() {
        val sensors = FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO

        val previous = AirQualityLog(1,
                3,
                PollutionDetails(2229222),
                400,
                0,
                1,
                1,
                SensorsPresence(sensors))

        val current = AirQualityLog(2,
                -1,
                PollutionDetails(9999933),
                400,
                0,
                2,
                1,
                SensorsPresence(sensors))

        val result = AQLogsComparer.compare(current, previous, 3)
        Assert.assertEquals(AQLogsComparer.RESULT_DATA_SHORTAGE_STARTED, result)
    }

    @Test
    fun ignoresShortageWhenItStartsWhenAirWasBadAnywayWithCurrentHavingKeyPollutants() {
        val sensors = FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO

        val previous = AirQualityLog(1,
                3,
                PollutionDetails(3339333),
                400,
                0,
                1,
                1,
                SensorsPresence(sensors))

        val current = AirQualityLog(2,
                3,
                PollutionDetails(3339933),
                400,
                0,
                2,
                1,
                SensorsPresence(sensors))

        val result = AQLogsComparer.compare(current, previous, 3)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresTransitionFromLikelyBadToPartBad() {
        val sensors = FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO

        val previous = AirQualityLog(1,
                3,
                PollutionDetails(3339933),
                400,
                0,
                1,
                1,
                SensorsPresence(sensors))

        val current = AirQualityLog(2,
                -1,
                PollutionDetails(9999933),
                400,
                0,
                2,
                1,
                SensorsPresence(sensors))

        val result = AQLogsComparer.compare(current, previous, 3)
        Assert.assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun noticesShortageWhenItHidesPotentialBadAir() {
        val sensors = FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO

        val previous = AirQualityLog(1,
                3,
                PollutionDetails(3339933),
                400,
                0,
                1,
                1,
                SensorsPresence(sensors))

        val current = AirQualityLog(2,
                -1,
                PollutionDetails(9999900),
                400,
                0,
                2,
                1,
                SensorsPresence(sensors))

        val result = AQLogsComparer.compare(current, previous, 3)
        Assert.assertEquals(AQLogsComparer.RESULT_DATA_SHORTAGE_STARTED, result)
    }
}