package com.github.pksokolowski.smogalert

import android.app.Application
import android.content.res.Resources
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.utils.AirQualityIndexHelper
import com.github.pksokolowski.smogalert.utils.SeasonalKeyPollutantsHelper
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.utils.anything
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
    fun showsColorWithPartialDataCoveringKeyPollutants(){
        val log = AirQualityLog(1, -1, PollutionDetails(3399999), 500, 0, 1, 1, SensorsPresence(127))
        seasonalPollutantsCovered = true
        val color = airQualityIndexHelper.getColor(log)

        assertEquals(3, color)
    }

    @Test
    fun showsNoDataColorWithPartialDataNotCoveringKeyPollutants(){
        val log = AirQualityLog(1, -1, PollutionDetails(3399999), 500, 0, 1, 1, SensorsPresence(127))
        seasonalPollutantsCovered = false
        val color = airQualityIndexHelper.getColor(log)

        assertEquals(9, color)
    }

    @Test
    fun showsCorrectIndexTitle(){
        val sensors = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3)
        val log = AirQualityLog(1, 4, PollutionDetails(3349999), 500, 0, 1, 1, sensors)
        seasonalPollutantsCovered = true
        val title = airQualityIndexHelper.getTitle(log)

        assertEquals("4", title)
    }

    @Test
    fun showsNoDataTitleWhenThereIsNoData(){
        val sensors = SensorsPresence(FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6)
        val log = AirQualityLog(1, -1, PollutionDetails(9999999), 10000, 0, 1, 3, sensors)
        seasonalPollutantsCovered = true
        val title = airQualityIndexHelper.getTitle(log)

        assertEquals("9", title)
    }

    @Test
    fun showsNoDataTitleWhenThereIsErrorAndPartialDataAndSeasonalKeyPollutantsArentCovered(){
        val sensors = SensorsPresence(FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6)
        val log = AirQualityLog(1, -1, PollutionDetails(9999949), 10000, 5, 1, 3, sensors)
        seasonalPollutantsCovered = false
        val title = airQualityIndexHelper.getTitle(log)

        assertEquals("9", title)
    }

    @Test
    fun showsNoDataTitleWhenThereIsErrorAndPartialDataAndSeasonalKeyPollutantsAreCovered(){
        val sensors = SensorsPresence(FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6)
        val log = AirQualityLog(1, -1, PollutionDetails(9499949), 10000, 5, 1, 3, sensors)
        seasonalPollutantsCovered = true
        val title = airQualityIndexHelper.getTitle(log)

        assertEquals("9", title)
    }

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockResources: Resources

    @Mock
    private lateinit var mockSeasonalHelper: SeasonalKeyPollutantsHelper

    /**
     * this will be returned by the mockSeasonalHelper when .coversKeyPollutantsIfExpected() is called
     */
    private var seasonalPollutantsCovered = true

    private lateinit var airQualityIndexHelper: AirQualityIndexHelper

    @Before
    fun prepareMocks() {
        `when`(mockSeasonalHelper.coversKeyPollutantsIfExpected(anything())).then { seasonalPollutantsCovered }

        `when`(mockApplication.resources).thenReturn(mockResources)
        `when`(mockResources.getStringArray(R.array.index_level_titles)).thenReturn(FAKE_INDEX_LEVEL_NAMES)
        `when`(mockResources.getIntArray(R.array.air_quality_index_colors)).thenReturn(FAKE_INDEX_COLORS)

        airQualityIndexHelper = AirQualityIndexHelper(mockSeasonalHelper, mockApplication)
    }

    private companion object {
        val FAKE_INDEX_LEVEL_NAMES = arrayOf("0", "1", "2", "3", "4", "5", "9")
        val FAKE_INDEX_COLORS = intArrayOf(0, 1, 2, 3, 4, 5, 9)
    }
}