package com.github.pksokolowski.smogalert.alarms

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.utils.TimeHelper
import java.util.*
import javax.inject.Inject

@PerApp
class AlarmHelper @Inject constructor(private val context: Application) {
    private val random = Random()

    private fun computeNextAlarmTimeFromNow(): Long {
        val c = Calendar.getInstance()
        val timeNow = c.timeInMillis

        // if it is past this hour's update time (minimum) then set alarm for the next hour
        if (TimeHelper.getCurrentMinuteOfHour() >= UPDATE_MINIMUM_MINUTE_OF_HOUR) {
            c.add(Calendar.HOUR, 1)
        }

        // to maintain same minute_of_our for subsequent elapsedTimeAlarms, difference between current
        // minute_of_hour and the desired one is calculated and applied, so the time till the alarm
        // will be this much more or less than an hour.
        val minutesDifference = UPDATE_MINIMUM_MINUTE_OF_HOUR - c.get(Calendar.MINUTE)
        c.add(Calendar.MINUTE, minutesDifference)

        // return time till alarm, or equivalently: time till UPDATE_MINIMUM_MINUTE_OF_HOUR or the
        // next hour.
        return c.timeInMillis - timeNow
    }

    fun setNext(): Long {
        val pending = getPendingIntent()

        val millisTillAlarm = computeNextAlarmTimeFromNow() + getJitter()
        val alarmTime = SystemClock.elapsedRealtime() + millisTillAlarm

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pending)
        } else {
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pending)
        }

        return millisTillAlarm
    }

    fun setAlarmsEnabled(enabled: Boolean) {
        BootFinishedReceiver.setBootFinishedReceiverEnabled(context, enabled)

        if (enabled) {
            setNext()
        } else {
            cancelAlarm()
        }
    }

    private fun getJitter() =
            random.nextInt(1000 * 60 * RANDOM_REQUEST_DELAY_MAX_MINUTES).toLong()

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(AlarmReceiver.ACTION_CHECK_AIR_QUALITY)
        intent.setClass(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun cancelAlarm() {
        val pending = getPendingIntent()
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pending)
    }

    private companion object {
        const val UPDATE_MINIMUM_MINUTE_OF_HOUR = 23
        const val RANDOM_REQUEST_DELAY_MAX_MINUTES = 15
    }
}