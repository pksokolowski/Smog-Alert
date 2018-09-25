package com.github.pksokolowski.smogalert.utils

class SensorsPresence(private val sensorFlags: Int = 0) {

    fun hasSensors(sensorFlags: Int) = containsSensors(this.sensorFlags, sensorFlags)

    fun combineWith(sensorFlags: Int) = SensorsPresence(combineSensors(this.sensorFlags, sensorFlags))

    companion object {

        fun containsSensors(container: Int, sensors: Int) = container and sensors != 0

        fun combineSensors(A: Int, B: Int) = A or B

        const val FLAG_SENSOR_PM10 = 1
        const val FLAG_SENSOR_PM25 = 2
        const val FLAG_SENSOR_O3 = 4
        const val FLAG_SENSOR_NO2 = 8
        const val FLAG_SENSOR_SO2 = 16
        const val FLAG_SENSOR_C6H6 = 32
        const val FLAG_SENSOR_CO = 64
    }
}