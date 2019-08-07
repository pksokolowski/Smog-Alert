package com.github.pksokolowski.smogalert.db

import androidx.room.TypeConverter
import com.github.pksokolowski.smogalert.utils.SensorsPresence

class Converters {
    @TypeConverter
    fun encodePollutionDetails(pollutionDetails: PollutionDetails): Int = pollutionDetails.encode()

    @TypeConverter
    fun decodePollutionDetails(value: Int): PollutionDetails =
            PollutionDetails(value)

    @TypeConverter
    fun encodeSensorsPresence(sensorsPresence: SensorsPresence): Int = sensorsPresence.sensorFlags

    @TypeConverter
    fun decodeSensorsPresence(value: Int): SensorsPresence =
            SensorsPresence(value)
}