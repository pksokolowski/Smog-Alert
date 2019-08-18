package com.github.pksokolowski.smogalert

import android.app.Application
import android.content.res.Resources
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.utils.ErrorExplanationHelper
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_CO
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_NO2
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_SO2
import com.github.pksokolowski.smogalert.utils.getTimestampFromMonth
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ErrorExplanationHelperTest {

    @Test
    fun handlesErrors() {
        for (errorCode in 1..5) {
            val log = AirQualityLog(errorCode = errorCode, timeStamp = 0)
            val result = errorExplanationHelper.explain(log)
            assertEquals(errorCode.toString(), result)
        }
    }

    @Test
    fun handlesPartialData() {
        val log = AirQualityLog(airQualityIndex = 1,
                details = PollutionDetails(0, 0, 0, 1, 0, -1, -1),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2 or FLAG_SENSOR_C6H6),
                timeStamp = 0)

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA
        assertEquals(expected, result)
    }

    @Test
    fun handlesPartialDataWithKeyPollutantsDataMissingButExpected() {
        val log = AirQualityLog(
                details = PollutionDetails(-1, 0, 1, 0, -1, -1, -1),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2),
                timeStamp = getTimestampFromMonth(10))

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA_MISSING_KEY_POLLUTANTS + log.details.getHighestIndex().toString()
        assertEquals(expected, result)
    }

    @Test
    fun handlesPartialDataWithKeyPollutantsDataPresentAndExpectedButO3MissingAndExpected() {
        val log = AirQualityLog(airQualityIndex = 0,
                details = PollutionDetails(1, 1, -1, 0, -1, -1, 0),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO),
                timeStamp = getTimestampFromMonth(11, 4))

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA
        assertEquals(expected, result)
    }

    @Test
    fun detectsMissingKeyPollutantPM25WhenPM10IsNotMissing() {
        val log = AirQualityLog(
                details = PollutionDetails(1, -1, -1, -1, -1, -1, -1),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25),
                timeStamp = getTimestampFromMonth(2, 12))

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA_MISSING_KEY_POLLUTANTS + log.details.getHighestIndex().toString()
        assertEquals(expected, result)
    }

    @Test
    fun handlesPartialDataWithPM10PresentButPM25Missing_BothRequired() {
        val log = AirQualityLog(
                details = PollutionDetails(2, -1, 1, 0, -1, -1, -1),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2),
                timeStamp = getTimestampFromMonth(2))

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA_MISSING_KEY_POLLUTANTS + log.details.getHighestIndex().toString()
        assertEquals(expected, result)
    }

    @Test
    fun handlesPartialDataWithO3MissingWhenRequiredAndExpected() {
        val log = AirQualityLog(airQualityIndex = -1,
                details = PollutionDetails(0, 0, -1, 0, -1, -1, -1),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2),
                timeStamp = getTimestampFromMonth(4))

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA_MISSING_KEY_POLLUTANTS + log.details.getHighestIndex().toString()
        assertEquals(expected, result)
    }

    @Test
    fun handlesPartialDataWithO3MissingWhenNotRequiredButExpected() {
        val log = AirQualityLog(airQualityIndex = 0,
                details = PollutionDetails(0, 0, -1, 0, -1, -1, -1),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2),
                timeStamp = getTimestampFromMonth(12))

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA
        assertEquals(expected, result)
    }

    @Test
    fun handlesPartialDataWithO3MissingWhenRequiredButNotExpected() {
        val log = AirQualityLog(airQualityIndex = -1,
                details = PollutionDetails(0, 0, -1, 0, -1, -1, -1),
                expectedSensorCoverage = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_NO2),
                timeStamp = getTimestampFromMonth(4))

        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA_MISSING_KEY_POLLUTANTS + log.details.getHighestIndex().toString()
        assertEquals(expected, result)
    }

    @Test
    fun handlesServerIssue() {
        val log = AirQualityLog(
                details = PollutionDetails(-1, -1, -1, -1, -1, -1, -1),
                timeStamp = 0)
        val result = errorExplanationHelper.explain(log)
        val expected = FAKE_ERROR_TO_EXPLANATION_MAP[R.string.error_explanation_server]
        assertEquals(expected, result)
    }

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockResources: Resources

    private lateinit var errorExplanationHelper: ErrorExplanationHelper

    @Before
    fun prepareMock() {
        `when`(mockApplication.getString(Mockito.anyInt())).thenAnswer { invocation -> FAKE_ERROR_TO_EXPLANATION_MAP[invocation.arguments[0] as Int] }
        `when`(mockApplication.resources).thenReturn(mockResources)
        `when`(mockResources.getStringArray(R.array.index_level_titles)).thenReturn(FAKE_INDEX_LEVEL_NAMES)
        `when`(mockApplication.getString(Mockito.anyInt(), Mockito.anyString())).thenAnswer { invocation ->
            val messageInt = invocation.arguments[0] as Int
            val message = if (messageInt == R.string.error_explanation_partial_data_without_key_pollutants) FAKE_EXPLANATION_PARTIAL_DATA_MISSING_KEY_POLLUTANTS
            else throw RuntimeException("unknown resource int")
            val level = invocation.arguments[1] as String
            message + level
        }

        errorExplanationHelper = ErrorExplanationHelper(mockApplication)
    }


    private companion object {
        val FAKE_INDEX_LEVEL_NAMES = arrayOf("0", "1", "2", "3", "4", "5", "9")
        const val FAKE_EXPLANATION_PARTIAL_DATA = "P"
        const val FAKE_EXPLANATION_PARTIAL_DATA_MISSING_KEY_POLLUTANTS = "PK"

        val FAKE_ERROR_TO_EXPLANATION_MAP = mapOf(
                R.string.error_explanation_internet to "1",
                R.string.error_explanation_location to "2",
                R.string.error_explanation_stations_missing to "3",
                R.string.error_explanation_stations_far_away to "4",
                R.string.error_explanation_connection to "5",
                R.string.error_explanation_unknown to "UNKNOWN",
                R.string.error_explanation_server to "SERVER",
                R.string.error_explanation_partial_data to FAKE_EXPLANATION_PARTIAL_DATA)
    }
}