package com.github.pksokolowski.smogalert.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import javax.inject.Inject

class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmHelper: AlarmHelper

    @Inject
    lateinit var airQualityChecker: AirQualityChecker

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action ?: return
        AndroidInjection.inject(this, context)

        if (action == ACTION_CHECK_AIR_QUALITY) {
            alarmHelper.setNext()
            airQualityChecker.check()
        }
    }

    companion object {
        const val ACTION_CHECK_AIR_QUALITY = "com.github.pksokolowski.smogalert.action.check_air_quality"
    }
}