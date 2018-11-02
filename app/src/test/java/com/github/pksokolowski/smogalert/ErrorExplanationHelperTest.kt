package com.github.pksokolowski.smogalert

import android.content.Context
import android.content.res.Resources
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.utils.ErrorExplanationHelper
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
        for(errorCode in 1..5) {
            val log = AirQualityLog(errorCode = errorCode, timeStamp = 0)
            val result = ErrorExplanationHelper.explain(log, mockContext)
            assertEquals(errorCode.toString(), result)
        }
    }

    @Test
    fun handlesPartialData() {
        val log = AirQualityLog(
                details = PollutionDetails(0, 0, 0, 1, 0, -1, -1),
                timeStamp = 0)

        val result = ErrorExplanationHelper.explain(log, mockContext)
        val expected = FAKE_EXPLANATION_PARTIAL_DATA + log.details.getHighestIndex().toString()
        assertEquals(expected, result)
    }

    @Test
    fun handlesServerIssue() {
        val log = AirQualityLog(
                details = PollutionDetails(-1, -1, -1, -1, -1, -1, -1),
                timeStamp = 0)
        val result = ErrorExplanationHelper.explain(log, mockContext)
        val expected = FAKE_ERROR_TO_EXPLANATION_MAP[R.string.error_explanation_server]
        assertEquals(expected, result)
    }

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    @Before
    fun prepareMock() {
        `when`(mockContext.getString(Mockito.anyInt())).thenAnswer { invocation -> FAKE_ERROR_TO_EXPLANATION_MAP[invocation.arguments[0] as Int] }
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.getStringArray(R.array.index_level_titles)).thenReturn(FAKE_INDEX_LEVEL_NAMES)
        `when`(mockContext.getString(Mockito.anyInt(), Mockito.anyString())).thenAnswer { invocation ->
            val level = invocation.arguments[1] as String
            FAKE_EXPLANATION_PARTIAL_DATA + level
        }
    }

    private companion object {
        val FAKE_ERROR_TO_EXPLANATION_MAP = mapOf(
                R.string.error_explanation_internet to "1",
                R.string.error_explanation_location to "2",
                R.string.error_explanation_stations_missing to "3",
                R.string.error_explanation_stations_far_away to "4",
                R.string.error_explanation_connection to "5",
                R.string.error_explanation_unknown to "UNKNOWN",
                R.string.error_explanation_server to "SERVER")

        val FAKE_INDEX_LEVEL_NAMES = arrayOf("0", "1", "2", "3", "4", "5", "9")
        const val FAKE_EXPLANATION_PARTIAL_DATA = "P"
    }
}