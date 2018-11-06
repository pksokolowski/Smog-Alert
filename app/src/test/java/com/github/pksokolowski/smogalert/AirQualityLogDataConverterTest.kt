package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.api.models.AirQualityModel
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.db.PollutionDetails.Companion.NUMBER_OF_POSSIBLE_SENSORS
import com.github.pksokolowski.smogalert.utils.AirQualityLogDataConverter
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_CO
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_NO2
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_SO2
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class AirQualityLogDataConverterTest {

    @Test
    fun recognizesPollutants() {
        for (i in SENSORS.indices) {
            val subIndexes = Array(NUMBER_OF_POSSIBLE_SENSORS){ if(it == i) 0 else -1}
            val model = getModel(subIndexes, 500, true)
            val log = AirQualityLogDataConverter.toAirQualityLog(model, 0, 0)

            val sensors = log.details.getSensorCoverage()
            assertEquals("thinks it does not have the sensor that was added to it", true, sensors.hasSensors(SENSORS[i]))

            for (s in SENSORS.indices) {
                if(s == i) continue
                if (sensors.hasSensors(SENSORS[s])) fail("reports it has sensors it does not have")
            }
        }
    }

    @Test
    fun recognizesWhenThereAreNoPollutantsCovered() {
        val model = getModel(PollutionDetails(), indexStatus = false)
        val log = AirQualityLogDataConverter.toAirQualityLog(model, 0, 0)
        for (s in SENSORS) {
            if (log.hasFlag(s)) fail()
        }
    }

    @Test
    fun recognizesWhenThereIsAnError() {
        val model = getModel(PollutionDetails(), null, null)
        val log = AirQualityLogDataConverter.toAirQualityLog(model, 0, 0)
       assertEquals(AirQualityLog.ERROR_CODE_AIR_QUALITY_MISSING, log.errorCode)
    }

//    @Test
//    fun recognizesVariousCombinationsOfPollutants() {
//        val rand = Random(3)
//        for (i in 0 until 50) {
//            var sensors = 0
//            val sensorsPicked = Array(7) { false }
//            for (rounds in 0 until rand.nextInt(20)) {
//                val chosenIndex = rand.nextInt(7)
//                sensors = sensors or SENSORS[chosenIndex]
//                sensorsPicked[chosenIndex] = true
//            }
//
//            val model = getModel(sensors)
//            val log = AirQualityLogDataConverter.toAirQualityLog(model, 0, 0)
//
//            for (s in SENSORS.indices) {
//                val sensor = SENSORS[s]
//                val has = log.hasFlag(sensor)
//                val shouldHave = sensorsPicked[s]
//                if (has != shouldHave) fail()
//            }
//        }
//
//    }

    private fun getModel(pollutionDetails: PollutionDetails, stationId: Int? = 11, indexStatus: Boolean? = true): AirQualityModel {
        val subIndexes = pollutionDetails.getDetailsArray()
        return getModel(subIndexes, stationId, indexStatus)
    }

    private fun getModel(subIndexes: Array<Int>, stationId: Int?, indexStatus: Boolean?): AirQualityModel {
        val model = AirQualityModel()
        model.id = stationId
        model.pm10 = getIndexOfNull(subIndexes[0])
        model.pm25 = getIndexOfNull(subIndexes[1])
        model.o3 = getIndexOfNull(subIndexes[2])
        model.no2 = getIndexOfNull(subIndexes[3])
        model.so2 = getIndexOfNull(subIndexes[4])
        model.c6h6 = getIndexOfNull(subIndexes[5])
        model.co = getIndexOfNull(subIndexes[6])
        model.status = indexStatus
        return model
    }

    private fun getIndexOfNull(value: Int): AirQualityModel.IndexLevel? {
        if (value != -1) {
            val indexLevel = AirQualityModel.IndexLevel()
            indexLevel.value = value
            return indexLevel
        } else
            return null
    }

    val SENSORS = listOf(
            FLAG_SENSOR_PM10,
            FLAG_SENSOR_PM25,
            FLAG_SENSOR_O3,
            FLAG_SENSOR_NO2,
            FLAG_SENSOR_SO2,
            FLAG_SENSOR_C6H6,
            FLAG_SENSOR_CO
    )
}