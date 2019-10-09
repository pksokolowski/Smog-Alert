package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.utils.SeasonalKeyPollutantsHelper
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
import org.junit.Test

class SeasonalKeyPollutantsHelperTest{

    @Test
    fun includesOzoneWhenMissingAtTheBeginningOfItsSeason(){
        val input = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25)
        val expected = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3)
        val result = seasonalHelper.includeKeyPollutants(input, getTimestampFromMonth(4, 1))

        assertEquals(expected, result)
    }

    @Test
    fun includesOzoneWhenMissingLaterOnInTheSeason(){
        val input = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25)
        val expected = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3)
        val result = seasonalHelper.includeKeyPollutants(input, getTimestampFromMonth(9, 30))

        assertEquals(expected, result)
    }

    @Test
    fun includesMissingKeyPollutantsAsNecessaryWhileRetainingOthers(){
        val input = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_NO2 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO)
        val expected = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2 or FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO)
        val result = seasonalHelper.includeKeyPollutants(input, getTimestampFromMonth(9))

        assertEquals(expected, result)
    }

    @Test
    fun includesAllWhenAllMissing(){
        val input = SensorsPresence()
        val expected = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3)
        val result = seasonalHelper.includeKeyPollutants(input, getTimestampFromMonth(9))

        assertEquals(expected, result)
    }

    @Test
    fun toleratesPM10MissingWhenPM25IsPresent(){
        val input = SensorsPresence(FLAG_SENSOR_PM25)
        val expected = SensorsPresence(FLAG_SENSOR_PM25)
        val result = seasonalHelper.includeKeyPollutants(input, getTimestampFromMonth(11))

        assertEquals(expected, result)
    }

    @Test
    fun doesNotToleratePMsMissingInSeason(){
        val input = SensorsPresence()
        val expected = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25)
        val result = seasonalHelper.includeKeyPollutants(input, getTimestampFromMonth(11))

        assertEquals(expected, result)
    }

    @Test
    fun findsKeyPollutantsPresenceWhenTheyArePresentButOtherPollutantsAreMissing(){
        val gained = SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3 or FLAG_SENSOR_NO2 or FLAG_SENSOR_SO2 or FLAG_SENSOR_CO)
        val expected = SensorsPresence(127)
        val result = seasonalHelper.coversKeyPollutantsIfExpected(gained, expected, getTimestampFromMonth(11, 11))

        assertEquals(result, true)
    }

    private val seasonalHelper = SeasonalKeyPollutantsHelper()

}