package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.utils.SensorsPresence
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import org.junit.Test

class SensorsPresenceTest {
    @Test
    fun detectsMissingSensor() {
        val gainedCoverage = SensorsPresence(95)
        val expectedCoverage = SensorsPresence(127)
        if (gainedCoverage.hasSensors(expectedCoverage)) fail("did not detect missing sensor")
    }

    @Test
    fun hasFlagsWorksCorrectlyWithAllFlagsCombinations() {
        for (i in 0..127) {
            for (ii in 0..127) {
                val sensA = SensorsPresence(i)
                val altA = AlternativeImplementation(i)

                val sensB = SensorsPresence(ii)
                val altB = AlternativeImplementation(ii)

                assertEquals("hasSensorsDiffered for: $sensA.hasSensors($sensB)", altA.hasFlags(altB), sensA.hasSensors(sensB))
            }
        }
    }

    @Test
    fun combineWithWorksCorrectlyWithAllFlagsCombinations() {
        for (i in 0..127) {
            for (ii in 0..127) {
                val sensA = SensorsPresence(i)
                val altA = AlternativeImplementation(i)

                val sensB = SensorsPresence(ii)
                val altB = AlternativeImplementation(ii)

                assertEquals("combinedWith results differed for: $sensA.combinedWith($sensB)", altA.combineWith(altB).toIntFlags(), extractFlagsInt(sensA.combinedWith(sensB)))
            }
        }
    }

    private fun extractFlagsInt(sensors: SensorsPresence): Int{
        var flags = 0
        for(i in SENSORS.indices){
            if (sensors.hasSensors(SENSORS[i])) flags = flags or SENSORS[i]
        }
        return flags
    }

    class AlternativeImplementation(private val sensorFlags: Int) {
        private val sensors = List(7) { sensorFlags and SENSORS[it] != 0 }

        fun combineWith(other: AlternativeImplementation): AlternativeImplementation {
            val combinedSensors = List(7) {
                sensors[it] || other.sensors[it]
            }
            return AlternativeImplementation(convertToIntFlags(combinedSensors))
        }

        fun hasFlags(other: AlternativeImplementation): Boolean {
            for (i in sensors.indices) {
                if (!other.sensors[i]) continue
                if (!sensors[i]) return false
            }
            return true
        }

        fun toIntFlags() = convertToIntFlags(sensors)

        override fun toString(): String {
            return convertToIntFlags(sensors).toString()
        }

        companion object {
            fun convertToIntFlags(sensorsList: List<Boolean>): Int {
                var flags = 0
                for (i in sensorsList.indices) {
                    val b = sensorsList[i]
                    if (b) flags = flags or SENSORS[i]
                }
                return flags
            }
        }
    }


    private companion object {
        val SENSORS = listOf(
                SensorsPresence.FLAG_SENSOR_PM10,
                SensorsPresence.FLAG_SENSOR_PM25,
                SensorsPresence.FLAG_SENSOR_O3,
                SensorsPresence.FLAG_SENSOR_NO2,
                SensorsPresence.FLAG_SENSOR_SO2,
                SensorsPresence.FLAG_SENSOR_C6H6,
                SensorsPresence.FLAG_SENSOR_CO
        )
    }
}