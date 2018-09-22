package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_CO
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_NO2
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_SO2
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.SENSORS
import junit.framework.Assert.*
import org.junit.Test

class AirQualityLogTest {

    @Test
    fun tellsIfHasPmSensors() {
        val log = AirQualityLog(timeStamp = 0, metadata = FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25)
        assertEquals(true, log.hasParticulateMatterData())

        val logWithJustOne = AirQualityLog(timeStamp = 0, metadata = FLAG_SENSOR_PM10 or FLAG_SENSOR_O3)
        assertEquals(true, logWithJustOne.hasParticulateMatterData())

        val logWithEverything = AirQualityLog(timeStamp = 0, metadata =
        FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_NO2 or FLAG_SENSOR_SO2 or FLAG_SENSOR_O3 or FLAG_SENSOR_C6H6 or FLAG_SENSOR_CO)
        assertEquals(true, logWithEverything.hasParticulateMatterData())
    }

    @Test
    fun admitsIfDoesNotHavePmSensors() {
        val log = AirQualityLog(1, 1, 530, 0, 0, FLAG_SENSOR_CO or FLAG_SENSOR_O3)
        val hasPMs = log.hasParticulateMatterData()
        assertEquals(false, hasPMs)
    }

    @Test
    fun countsSensorsCorrectly() {
        for (i in 0..6) {
            var sensors = 0
            for (ii in 0 until i) {
                sensors = sensors or SENSORS[ii]
            }
            val log = AirQualityLog(timeStamp = 0, metadata = sensors)
            assertEquals(i, log.getSensorCount())
        }
    }
}