package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.job.AQLogsComparer
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
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_OK_AFTER_SHORTAGE_ENDED, result)
    }

    @Test
    fun detectsBadAirRightAfterDataShortage() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        Assert.assertEquals(AQLogsComparer.RESULT_DEGRADED_PAST_THRESHOLD, result)
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
}