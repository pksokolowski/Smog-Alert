package com.github.pksokolowski.smogalert.utils

import java.text.SimpleDateFormat
import java.util.*

class TimeHelper{
    companion object {
        fun getCurrentMinuteOfHour(): Int {
            return Calendar.getInstance().get(Calendar.MINUTE)
        }

        fun getTimeStampString(millis: Long): String {
            val formatter = SimpleDateFormat("HH:mm  dd.MM.yy", Locale.US)
            // formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            return formatter.format(Date(millis))
        }
    }
}