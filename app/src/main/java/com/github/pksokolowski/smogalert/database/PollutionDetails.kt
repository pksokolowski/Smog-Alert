package com.github.pksokolowski.smogalert.database

import com.github.pksokolowski.smogalert.utils.SensorsPresence
import java.lang.RuntimeException

class PollutionDetails {
    private val pollutionData: Array<Int>

    constructor() : this(Array<Int>(NUMBER_OF_POSSIBLE_SENSORS) { -1 })

    constructor(encodedInt: Int) {
        pollutionData = Array(NUMBER_OF_POSSIBLE_SENSORS) { 9 }

        var encoded = encodedInt
        for (i in pollutionData.size - 1 downTo 0) {
            val digit = encoded.rem(10)
            pollutionData[i] = if (digit == 9) -1 else digit
            encoded /= 10
        }
    }

    constructor(pm10: Int, pm25: Int, o3: Int, no2: Int, so2: Int, c6h6: Int, co: Int) : this(
            arrayOf(pm10, pm25, o3, no2, so2, c6h6, co))

    private constructor(detailsArray: Array<Int>) {
        pollutionData = Array(NUMBER_OF_POSSIBLE_SENSORS) { -1 }
        for (i in detailsArray.indices) {
            val subIndexLevel = detailsArray[i]
            if (subIndexLevel > 5 || subIndexLevel < -1)
                throw RuntimeException("subIndexLevel larger than allowed")
            pollutionData[i] = subIndexLevel
        }
    }

    fun getDetailsArray(): Array<Int> = Array(pollutionData.size) { pollutionData[it] }

    fun getHighestIndex(): Int {
        var highest = -1
        for (i in pollutionData) {
            if (i > highest) highest = i
        }
        return highest
    }

    fun encode(): Int {
        var encoded = 0
        for (i in pollutionData.indices) {
            val valueTuUse = if (pollutionData[i] == -1) 9 else pollutionData[i]
            val multiplier = Math.pow(10.0, ((NUMBER_OF_POSSIBLE_SENSORS - 1.0) - i)).toInt()
            encoded += valueTuUse * multiplier
        }
        return encoded
    }

    fun combinedWith(details: PollutionDetails): PollutionDetails {
        val base = this.getDetailsArray()
        val addition = details.getDetailsArray()
        val combined = Array(base.size) { -1 }
        for (i in base.indices) {
            if (base[i] != -1) {
                combined[i] = base[i]
                continue
            }
            combined[i] = addition[i]
        }
        return PollutionDetails(combined)
    }

    fun getSensorCoverage(): SensorsPresence {
        val detailsArray = getDetailsArray()
        var sensorFlags = 0
        for (i in detailsArray.indices) {
            if (detailsArray[i] == -1) continue
            sensorFlags = sensorFlags or SENSORS_PRESENCE_FLAGS_IN_LOCAL_ORDER[i]
        }
        return SensorsPresence(sensorFlags)
    }

    companion object {
        //const val LEVEL_UNKNOWN = 9
        const val NUMBER_OF_POSSIBLE_SENSORS = 7

        /**
         * these are the flags used in SensorsPresence ordered as pollutants are ordered in
         * PollutionDetails. In case SensorsPresence should change it's ordering, this class won't
         * break.
         */
        private val SENSORS_PRESENCE_FLAGS_IN_LOCAL_ORDER = arrayOf(
                SensorsPresence.FLAG_SENSOR_PM10,
                SensorsPresence.FLAG_SENSOR_PM25,
                SensorsPresence.FLAG_SENSOR_O3,
                SensorsPresence.FLAG_SENSOR_NO2,
                SensorsPresence.FLAG_SENSOR_SO2,
                SensorsPresence.FLAG_SENSOR_C6H6,
                SensorsPresence.FLAG_SENSOR_CO
        )
    }

    override fun toString(): String {
        return pollutionData.joinToString(prefix = "[", postfix = "]")
    }
}