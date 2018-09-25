package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.TypeConverter

class Converters {
    @TypeConverter
    fun calendarToDatestamp(pollutionDetails: PollutionDetails): Int = pollutionDetails.encode()

    @TypeConverter
    fun datestampToCalendar(value: Int): PollutionDetails =
            PollutionDetails(value)
}