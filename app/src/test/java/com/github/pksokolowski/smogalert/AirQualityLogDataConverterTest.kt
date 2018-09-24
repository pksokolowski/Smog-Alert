package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.airquality.models.AirQualityModel
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_CO
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_NO2
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_SENSOR_SO2
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.SENSORS
import com.github.pksokolowski.smogalert.utils.AirQualityLogDataConverter
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import org.junit.Test
import java.util.*

class AirQualityLogDataConverterTest {

    @Test
    fun recognizesPollutants() {
        for (sensor in AirQualityLog.SENSORS) {
            val model = getModel(sensor)
            val log = AirQualityLogDataConverter.toAirQualityLog(model, 0, 0)

            assertEquals(true, log.hasFlag(sensor))

            for (s in SENSORS) {
                if (log.hasFlag(s) && s != sensor) fail()
            }
        }
    }

    @Test
    fun recognizesWhenThereAreNoPollutantsCovered(){
        val model = getModel(0)
        val log = AirQualityLogDataConverter.toAirQualityLog(model, 0, 0)
        for (s in SENSORS) {
            if(log.hasFlag(s)) fail()
        }
    }

    @Test
    fun recognizesVariousCombinationsOfPollutants() {
        val rand = Random(3)
        for (i in 0 until 50) {
            var sensors = 0
            val sensorsPicked = Array(7) { false }
            for (rounds in 0 until rand.nextInt(20)) {
                val chosenIndex = rand.nextInt(7)
                sensors = sensors or SENSORS[chosenIndex]
                sensorsPicked[chosenIndex] = true
            }

            val model = getModel(sensors)
            val log = AirQualityLogDataConverter.toAirQualityLog(model, 0, 0)

            for (s in SENSORS.indices) {
                val sensor = SENSORS[s]
                val has = log.hasFlag(sensor)
                val shouldHave = sensorsPicked[s]
                if (has != shouldHave) fail()
            }
        }

    }

    private fun getModel(sensors: Int): AirQualityModel {
        val model = AirQualityModel()
        model.pm10 = getIndexOfNull(sensors, FLAG_SENSOR_PM10)
        model.pm25 = getIndexOfNull(sensors, FLAG_SENSOR_PM25)
        model.o3 = getIndexOfNull(sensors, FLAG_SENSOR_O3)
        model.no2 = getIndexOfNull(sensors, FLAG_SENSOR_NO2)
        model.so2 = getIndexOfNull(sensors, FLAG_SENSOR_SO2)
        model.c6h6 = getIndexOfNull(sensors, FLAG_SENSOR_C6H6)
        model.co = getIndexOfNull(sensors, FLAG_SENSOR_CO)
        return model
    }

    private fun getIndexOfNull(sensors: Int, specificPollutant: Int): AirQualityModel.IndexLevel? {
        if (sensors and specificPollutant != 0) {
            val indexLevel = AirQualityModel.IndexLevel()
            indexLevel.value = 0
            return indexLevel
        } else
            return null
    }
}