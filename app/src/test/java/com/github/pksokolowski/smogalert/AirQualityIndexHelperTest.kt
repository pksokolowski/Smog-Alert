package com.github.pksokolowski.smogalert

import android.app.Application
import android.content.res.Resources
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.utils.AirQualityIndexHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AirQualityIndexHelperTest {

    @Test
    fun showsCorrectColor(){
        val log = AirQualityLog(1,  3, PollutionDetails(3399999), 500, 0, 1, 1)
        val color = airQualityIndexHelper.getColor(log)

        assertEquals(3, color)
    }

    @Test
    fun showsCorrectIndexTitle(){
        val log = AirQualityLog(1, 4, PollutionDetails(3349999), 500, 0, 1, 1)
        val title = airQualityIndexHelper.getTitle(log)

        assertEquals("4", title)
    }

    @Test
    fun showsNoDataTitleWhenThereIsNoData(){
        val log = AirQualityLog(1, -1, PollutionDetails(9999999), 10000, 0, 1, 3)
        val title = airQualityIndexHelper.getTitle(log)

        assertEquals("9", title)
    }

    @Test
    fun showsNoDataTitleWhenThereIsErrorAndPartialDataAndSeasonalKeyPollutantsAreCovered(){
        val log = AirQualityLog(1, -1, PollutionDetails(9499949), 10000, 5, 1, 3)
        val title = airQualityIndexHelper.getTitle(log)

        assertEquals("9", title)
    }

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockResources: Resources

    private lateinit var airQualityIndexHelper: AirQualityIndexHelper

    @Before
    fun prepareMocks() {
        `when`(mockApplication.resources).thenReturn(mockResources)
        `when`(mockResources.getStringArray(R.array.index_level_titles)).thenReturn(FAKE_INDEX_LEVEL_NAMES)
        `when`(mockResources.getIntArray(R.array.air_quality_index_colors)).thenReturn(FAKE_INDEX_COLORS)

        airQualityIndexHelper = AirQualityIndexHelper(mockApplication)
    }

    private companion object {
        val FAKE_INDEX_LEVEL_NAMES = arrayOf("0", "1", "2", "3", "4", "5", "9")
        val FAKE_INDEX_COLORS = intArrayOf(0, 1, 2, 3, 4, 5, 9)
    }
}