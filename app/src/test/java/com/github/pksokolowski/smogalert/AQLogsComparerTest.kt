package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.job.AQLogsComparer
import org.junit.Assert.assertEquals
import org.junit.Test

class AQLogsComparerTest {
    @Test
    fun ignoresNoChangeCaseWithGoodAir() {
        // no change case, good index
        val previous = AirQualityLog(2, 0, 1, 0, 2)
        val current = AirQualityLog(1, 0, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresNoChangeCaseWithBadAir() {
        // no change, bad index
        val previous = AirQualityLog(2, 5, 1, 0, 2)
        val current = AirQualityLog(1, 5, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresImprovementWhenNotCrossingThreshold() {
        // air degrades but not crossing the threshold (still bad)
        val previous = AirQualityLog(2, 4, 1, 0, 2)
        val current = AirQualityLog(1, 5, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsDegradation() {
        // air degraded, crossing threshold
        val previous = AirQualityLog(2, 3, 1, 0, 2)
        val current = AirQualityLog(1, 4, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_DEGRADED_PAST_THRESHOLD, result)
    }

    @Test
    fun detectsImprovementPastThreshold() {
        // improvement, crossing threshold
        val previous = AirQualityLog(2, 4, 1, 0, 2)
        val current = AirQualityLog(1, 3, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_IMPROVED_PAST_THRESHOLD, result)
    }


    @Test
    fun ignoresRepeatingError() {
        // location is missing twice in a row. Only the first instance of such error is to be noticed.
        val previous = AirQualityLog(2, 4, 1, AirQualityLog.ERROR_CODE_LOCATION_MISSING, 2)
        val current = AirQualityLog(1, 3, 1, AirQualityLog.ERROR_CODE_LOCATION_MISSING, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsNewErrors() {
        // location is missing twice in a row
        val previous = AirQualityLog(2, 4, 1, 0, 2)
        val current = AirQualityLog(1, 3, 1, AirQualityLog.ERROR_CODE_LOCATION_MISSING, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_ERROR_EMERGED, result)
    }

    @Test
    fun detectsNewDataShortage() {
        val previous = AirQualityLog(2, 0, 1, 0, 2)
        val current = AirQualityLog(1, -1, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_DATA_SHORTAGE_STARTED, result)
    }

    @Test
    fun ignoresRepetitiveDataShortage() {
        val previous = AirQualityLog(2, -1, 1, 0, 2)
        val current = AirQualityLog(1, -1, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresDataShortageEnd() {
        val previous = AirQualityLog(2, -1, 1, 0, 2)
        val current = AirQualityLog(1, 3, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsBadAirRightAfterDataShortage() {
        val previous = AirQualityLog(2, -1, 1, 0, 2)
        val current = AirQualityLog(1, 5, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_DEGRADED_PAST_THRESHOLD, result)
    }

    @Test
    fun ignoresGoodAirWhenPreviousLogIsNull() {
        // no change case, good index
        val previous = null
        val current = AirQualityLog(1, 0, 1, 0, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsNewErrorWhenPreviousLogIsNull() {
        // no change case, good index
        val previous = null
        val current = AirQualityLog(1, 0, 1, AirQualityLog.ERROR_CODE_LOCATION_MISSING, 1)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_ERROR_EMERGED, result)
    }

    @Test
    fun ignoresCaseWhereBothLogsAreNull() {
        // no change case, both null
        val previous = null
        val current = null

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_NO_INTERPRETATION, result)
    }
}