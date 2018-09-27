package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.AQLogsComparerTest.SimpleAQLogCategory.*
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.database.PollutionDetails
import com.github.pksokolowski.smogalert.job.AQLogsComparer
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DATA_SHORTAGE_STARTED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DEGRADED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_ERROR_EMERGED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_IMPROVED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_NO_INTERPRETATION
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_OK_AFTER_SHORTAGE_ENDED
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.StringBuilder

class AQLogsComparerTest {

    @Test
    fun handlesAllCombinationsAsExpected() {
        val cases = listOf(
                Case(NULL, PART_OK, RESULT_DATA_SHORTAGE_STARTED),
                Case(NULL, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(NULL, OK, RESULT_NO_INTERPRETATION),
                Case(NULL, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(NULL, UNKNOWN, RESULT_DATA_SHORTAGE_STARTED),
                Case(NULL, NULL, RESULT_NO_INTERPRETATION),
                Case(NULL, ERROR, RESULT_ERROR_EMERGED),

                Case(PART_OK, PART_OK, RESULT_NO_INTERPRETATION),
                Case(PART_OK, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(PART_OK, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(PART_OK, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(PART_OK, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(PART_OK, NULL, RESULT_NO_INTERPRETATION),
                Case(PART_OK, ERROR, RESULT_ERROR_EMERGED),

                Case(PART_BAD, PART_OK, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, PART_BAD, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(PART_BAD, BAD, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, NULL, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, ERROR, RESULT_ERROR_EMERGED),

                Case(OK, PART_OK, RESULT_DATA_SHORTAGE_STARTED),
                Case(OK, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(OK, OK, RESULT_NO_INTERPRETATION),
                Case(OK, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(OK, UNKNOWN, RESULT_DATA_SHORTAGE_STARTED),
                Case(OK, NULL, RESULT_NO_INTERPRETATION),
                Case(OK, ERROR, RESULT_ERROR_EMERGED),

                Case(BAD, PART_OK, RESULT_DATA_SHORTAGE_STARTED),
                Case(BAD, PART_BAD, RESULT_DATA_SHORTAGE_STARTED),
                Case(BAD, OK, RESULT_IMPROVED_PAST_THRESHOLD),
                Case(BAD, BAD, RESULT_NO_INTERPRETATION),
                Case(BAD, UNKNOWN, RESULT_DATA_SHORTAGE_STARTED),
                Case(BAD, NULL, RESULT_NO_INTERPRETATION),
                Case(BAD, ERROR, RESULT_ERROR_EMERGED),

                Case(UNKNOWN, PART_OK, RESULT_NO_INTERPRETATION),
                Case(UNKNOWN, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(UNKNOWN, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(UNKNOWN, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(UNKNOWN, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(UNKNOWN, NULL, RESULT_NO_INTERPRETATION),
                Case(UNKNOWN, ERROR, RESULT_ERROR_EMERGED),

                Case(ERROR, PART_OK, RESULT_NO_INTERPRETATION),
                Case(ERROR, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(ERROR, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(ERROR, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(ERROR, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(ERROR, NULL, RESULT_NO_INTERPRETATION),
                Case(ERROR, ERROR, RESULT_NO_INTERPRETATION)
        )

        runCases(cases)
    }

    private fun runCases(cases: List<Case>) {
        var errorsCount = 0
        val errors = StringBuilder().apply { appendln() }

        for (case in cases) {
            val actual = AQLogsComparer.compare(case.current.log, case.previous.log, 4)
            if (actual != case.expectedResult) {
                errors.appendln("$case got $actual")
                errorsCount++
            }
        }
        errors.appendln("errors count: $errorsCount")
        if (errorsCount > 0) fail(errors.toString())
    }

    private data class Case(val previous: SimpleAQLogCategory, val current: SimpleAQLogCategory, val expectedResult: Int)
    private enum class SimpleAQLogCategory(val log: AirQualityLog?) {
        PART_OK(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999990), timeStamp = 0)),
        PART_BAD(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999995), timeStamp = 0)),
        OK(AirQualityLog(airQualityIndex = 0, details = PollutionDetails(0), timeStamp = 0)),
        BAD(AirQualityLog(airQualityIndex = 5, details = PollutionDetails(5555555), timeStamp = 0)),
        UNKNOWN(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999999), timeStamp = 0)),
        NULL(null),
        ERROR(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999999), timeStamp = 0, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING))
    }

    @Test
    fun ignoresNoChangeCaseWithGoodAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 0, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 0, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresNoChangeCaseWithBadAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 5, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresDegradationWhenNotCrossingThreshold() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresDegradationWhenNotCrossingThresholdAndWithGoodAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 2, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun ignoresImprovementWhenNotCrossingThresholdAndWithGoodAir() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 3, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 1, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsDegradation() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 3, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 4, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_DEGRADED_PAST_THRESHOLD, result)
    }

    @Test
    fun detectsImprovementPastThreshold() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_IMPROVED_PAST_THRESHOLD, result)
    }


    @Test
    fun ignoresRepeatingError() {
        // location is missing twice in a row. Only the first instance of such error is to be noticed.
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsNewErrors() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 4, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_ERROR_EMERGED, result)
    }

    @Test
    fun detectsNewDataShortage() {
        val previous = AirQualityLog(id = 1, airQualityIndex = 0, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = -1, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_DATA_SHORTAGE_STARTED, result)
    }

    @Test
    fun ignoresRepetitiveDataShortage() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = -1, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsDataShortageEndWhenAirIsOk() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 3, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_OK_AFTER_SHORTAGE_ENDED, result)
    }

    @Test
    fun detectsBadAirRightAfterDataShortage() {
        val previous = AirQualityLog(id = 1, airQualityIndex = -1, timeStamp = 1)
        val current = AirQualityLog(id = 2, airQualityIndex = 5, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_DEGRADED_PAST_THRESHOLD, result)
    }

    @Test
    fun ignoresGoodAirWhenPreviousLogIsNull() {
        // no change case, good index
        val previous = null
        val current = AirQualityLog(id = 2, airQualityIndex = 0, timeStamp = 2)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }

    @Test
    fun detectsNewErrorWhenPreviousLogIsNull() {
        // no change case, good index
        val previous = null
        val current = AirQualityLog(id = 2, airQualityIndex = 0, timeStamp = 2, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING)

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(AQLogsComparer.RESULT_ERROR_EMERGED, result)
    }

    @Test
    fun ignoresCaseWhereBothLogsAreNull() {
        // no change case, both null
        val previous = null
        val current = null

        val result = AQLogsComparer.compare(current, previous, 4)
        assertEquals(RESULT_NO_INTERPRETATION, result)
    }
}