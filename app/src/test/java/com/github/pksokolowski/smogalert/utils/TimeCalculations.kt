package com.github.pksokolowski.smogalert.utils

import java.util.*

fun getTimestampFromMonth(monthNumber: Int, dayNum: Int = 10): Long {
    val cal = Calendar.getInstance(Locale.US).apply {
        timeInMillis = 0
        set(Calendar.MONTH, monthNumber - 1)
        set(Calendar.DAY_OF_MONTH, dayNum)
    }
    return cal.timeInMillis
}