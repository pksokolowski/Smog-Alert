package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.TypeConverter

class Converters {
    @TypeConverter
    fun encodePollutionDetails(pollutionDetails: PollutionDetails): Int = pollutionDetails.encode()

    @TypeConverter
    fun decodePollutionDetails(value: Int): PollutionDetails =
            PollutionDetails(value)
}