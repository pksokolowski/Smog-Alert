package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.AQLogsComparerExhaustiveTest.SimpleAQLogCategory.*
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.job.AQLogsComparer
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DATA_SHORTAGE_STARTED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DEGRADED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_ERROR_EMERGED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_IMPROVED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_LIKELY_OK
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_NO_INTERPRETATION
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_OK_AFTER_SHORTAGE_ENDED
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import org.junit.Assert.fail
import org.junit.Test
import java.lang.StringBuilder

class AQLogsComparerExhaustiveTest {

    @Test
    fun handlesAllCombinationsAsExpected() {
        runCases(
                Case(NULL, PART_OK, RESULT_DATA_SHORTAGE_STARTED),
                Case(NULL, LIKELY_OK, RESULT_LIKELY_OK),
                Case(NULL, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(NULL, OK, RESULT_NO_INTERPRETATION),
                Case(NULL, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(NULL, UNKNOWN, RESULT_DATA_SHORTAGE_STARTED),
                Case(NULL, NULL, RESULT_NO_INTERPRETATION),
                Case(NULL, ERROR, RESULT_ERROR_EMERGED),

                Case(PART_OK, PART_OK, RESULT_NO_INTERPRETATION),
                Case(PART_OK, LIKELY_OK, RESULT_LIKELY_OK),
                Case(PART_OK, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(PART_OK, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(PART_OK, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(PART_OK, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(PART_OK, NULL, RESULT_NO_INTERPRETATION),
                Case(PART_OK, ERROR, RESULT_ERROR_EMERGED),

                Case(LIKELY_OK, PART_OK, RESULT_DATA_SHORTAGE_STARTED),
                Case(LIKELY_OK, LIKELY_OK, RESULT_NO_INTERPRETATION),
                Case(LIKELY_OK, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(LIKELY_OK, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(LIKELY_OK, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(LIKELY_OK, UNKNOWN, RESULT_DATA_SHORTAGE_STARTED),
                Case(LIKELY_OK, NULL, RESULT_NO_INTERPRETATION),
                Case(LIKELY_OK, ERROR, RESULT_ERROR_EMERGED),

                Case(PART_BAD, PART_OK, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, LIKELY_OK, RESULT_LIKELY_OK),
                Case(PART_BAD, PART_BAD, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(PART_BAD, BAD, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, NULL, RESULT_NO_INTERPRETATION),
                Case(PART_BAD, ERROR, RESULT_ERROR_EMERGED),

                Case(OK, PART_OK, RESULT_DATA_SHORTAGE_STARTED),
                Case(OK, LIKELY_OK, RESULT_LIKELY_OK),
                Case(OK, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(OK, OK, RESULT_NO_INTERPRETATION),
                Case(OK, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(OK, UNKNOWN, RESULT_DATA_SHORTAGE_STARTED),
                Case(OK, NULL, RESULT_NO_INTERPRETATION),
                Case(OK, ERROR, RESULT_ERROR_EMERGED),

                Case(BAD, PART_OK, RESULT_DATA_SHORTAGE_STARTED),
                Case(BAD, LIKELY_OK, RESULT_LIKELY_OK),
                Case(BAD, PART_BAD, RESULT_DATA_SHORTAGE_STARTED),
                Case(BAD, OK, RESULT_IMPROVED_PAST_THRESHOLD),
                Case(BAD, BAD, RESULT_NO_INTERPRETATION),
                Case(BAD, UNKNOWN, RESULT_DATA_SHORTAGE_STARTED),
                Case(BAD, NULL, RESULT_NO_INTERPRETATION),
                Case(BAD, ERROR, RESULT_ERROR_EMERGED),

                Case(UNKNOWN, PART_OK, RESULT_NO_INTERPRETATION),
                Case(UNKNOWN, LIKELY_OK, RESULT_LIKELY_OK),
                Case(UNKNOWN, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(UNKNOWN, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(UNKNOWN, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(UNKNOWN, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(UNKNOWN, NULL, RESULT_NO_INTERPRETATION),
                Case(UNKNOWN, ERROR, RESULT_ERROR_EMERGED),

                Case(ERROR, PART_OK, RESULT_NO_INTERPRETATION),
                Case(ERROR, LIKELY_OK, RESULT_LIKELY_OK),
                Case(ERROR, PART_BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(ERROR, OK, RESULT_OK_AFTER_SHORTAGE_ENDED),
                Case(ERROR, BAD, RESULT_DEGRADED_PAST_THRESHOLD),
                Case(ERROR, UNKNOWN, RESULT_NO_INTERPRETATION),
                Case(ERROR, NULL, RESULT_NO_INTERPRETATION),
                Case(ERROR, ERROR, RESULT_NO_INTERPRETATION)
        )
    }

    private fun runCases(vararg cases: Case) {
        var errorsCount = 0
        val errors = StringBuilder().apply { appendln() }
        errors.appendln("Failed for the following cases: ")

        for (case in cases) {
            val actual = AQLogsComparer.compare(case.current.log, case.previous.log, 4)
            if (actual != case.expectedResult) {
                errors.appendln("${case.previous} ${case.current}  expected  ${translateResult(case.expectedResult)}  got  ${translateResult(actual)}")
                errorsCount++
            }
        }
        errors.appendln("errors count: $errorsCount")
        if (errorsCount > 0) fail(errors.toString())
    }

    private data class Case(val previous: SimpleAQLogCategory, val current: SimpleAQLogCategory, val expectedResult: Int)
    private enum class SimpleAQLogCategory(val log: AirQualityLog?) {
        PART_OK(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999990), timeStamp = 0, expectedSensorCoverage = SensorsPresence(127))),
        LIKELY_OK(AirQualityLog(airQualityIndex = 1, details = PollutionDetails(1119999), timeStamp = 0, expectedSensorCoverage = SensorsPresence(127))),
        PART_BAD(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999995), timeStamp = 0, expectedSensorCoverage = SensorsPresence(127))),
        OK(AirQualityLog(airQualityIndex = 0, details = PollutionDetails(0), timeStamp = 0, expectedSensorCoverage = SensorsPresence(127))),
        BAD(AirQualityLog(airQualityIndex = 5, details = PollutionDetails(5555555), timeStamp = 0, expectedSensorCoverage = SensorsPresence(127))),
        UNKNOWN(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999999), timeStamp = 0, expectedSensorCoverage = SensorsPresence(127))),
        NULL(null),
        ERROR(AirQualityLog(airQualityIndex = -1, details = PollutionDetails(9999999), timeStamp = 0, errorCode = AirQualityLog.ERROR_CODE_LOCATION_MISSING))
    }

    private fun translateResult(code: Int) = when (code) {
        RESULT_NO_INTERPRETATION -> "NO_INTERPRETATION"
        RESULT_DEGRADED_PAST_THRESHOLD -> "DEGRADED_PAST_THRESHOLD"
        RESULT_IMPROVED_PAST_THRESHOLD -> "IMPROVED_PAST_THRESHOLD"
        RESULT_OK_AFTER_SHORTAGE_ENDED -> "RESULT_OK_AFTER_SHORTAGE_ENDED"
        RESULT_ERROR_EMERGED -> "RESULT_ERROR_EMERGED"
        RESULT_DATA_SHORTAGE_STARTED -> "RESULT_DATA_SHORTAGE_STARTED"
        else -> "unknownCode"
    }
}