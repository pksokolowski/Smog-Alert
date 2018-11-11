package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.FLAG_USED_ACTIVE_LOCATION_METHOD
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.FLAG_USED_API
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import org.junit.Assert.assertEquals
import org.junit.Test

class AirQualityLogTest{
    @Test
    fun assignsIdKeepingEverythingElseTheSame(){
        val log = AirQualityLog(0, 4, PollutionDetails(3434343), 123, 1, 100, 1, SensorsPresence(32))
        val expected = AirQualityLog(52, 4, PollutionDetails(3434343), 123, 1, 100, 1, SensorsPresence(32))

        val result = log.assignId(52)
        assertEquals(expected, result)
    }

    @Test
    fun knowsWhatFlagsItHasOneByOne(){
        val log = AirQualityLog(5,
                5,
                PollutionDetails(3555555),
                100,
                4,
                233,
                FLAG_USED_API or FLAG_USED_ACTIVE_LOCATION_METHOD,
                SensorsPresence(32))

        assertEquals(true, log.hasFlags(FLAG_USED_API))
    }

    @Test
    fun knowsWhatFlagsItDoesNotHaveOneByOne(){
        val log = AirQualityLog(5,
                5,
                PollutionDetails(3555555),
                100,
                4,
                233,
                FLAG_USED_ACTIVE_LOCATION_METHOD,
                SensorsPresence(32))

        assertEquals(false, log.hasFlags(FLAG_USED_API))
    }

    @Test
    fun knowsWhatFlagsItHasOneByOneWhenAskedAboutAnotherFlag(){
        val log = AirQualityLog(5,
                5,
                PollutionDetails(3555555),
                100,
                4,
                233,
                FLAG_USED_API,
                SensorsPresence(32))

        assertEquals(false, log.hasFlags(FLAG_USED_ACTIVE_LOCATION_METHOD))
    }

    @Test
    fun handlesMultipleFlagsAtOnce(){
        val log = AirQualityLog(5,
                5,
                PollutionDetails(3555555),
                100,
                4,
                233,
                FLAG_USED_API,
                SensorsPresence(32))

        val flags = FLAG_USED_ACTIVE_LOCATION_METHOD or FLAG_USED_API
        assertEquals(false, log.hasFlags(flags))
    }

    @Test
    fun knowsWhenCoversTheExpectations(){
        val log = AirQualityLog(5,
                5,
                PollutionDetails(1919919),
                100,
                0,
                233,
                FLAG_USED_API,
                SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6))

        assertEquals(true, log.hasExpectedCoverage())
    }

    @Test
    fun knowsWhenDoesNotCoverTheExpectations(){
        val log = AirQualityLog(5,
                5,
                PollutionDetails(9919919),
                100,
                0,
                233,
                FLAG_USED_API,
                SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6))

        assertEquals(false, log.hasExpectedCoverage())
    }

    @Test
    fun recognizesWhenItDoesNotHaveIndex(){
        val log = AirQualityLog(5,
                -1,
                PollutionDetails(9919919),
                100,
                0,
                233,
                FLAG_USED_API,
                SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6))

        assertEquals(false, log.hasIndex())
    }

    @Test
    fun recognizesWhenItHasIndex(){
        val log = AirQualityLog(5,
                2,
                PollutionDetails(2219919),
                100,
                0,
                233,
                FLAG_USED_API,
                SensorsPresence(FLAG_SENSOR_PM10 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6))

        assertEquals(true, log.hasIndex())
    }
}