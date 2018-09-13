package com.github.pksokolowski.smogalert.alarms

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import android.os.PowerManager
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.notifications.NotificationHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import javax.inject.Inject

@PerApp
class AirQualityChecker @Inject constructor(private val context: Application,
                                            private val airQualityLogsRepository: AirQualityLogsRepository,
                                            private val notificationHelper: NotificationHelper) {

    fun check() {
        val airQualityCheckerTask = AirQualityCheckerTask(context, airQualityLogsRepository, notificationHelper)
        airQualityCheckerTask.execute()
    }


    private class AirQualityCheckerTask(context: Application,
                                        private val airQualityLogsRepository: AirQualityLogsRepository,
                                        private val notificationHelper: NotificationHelper) : AsyncTask<Void, Void, AirQualityLog>() {

        private val wakeLock: PowerManager.WakeLock

        init {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmogAlert_Partial_AirQualityCheck")
        }

        override fun onPreExecute() {
            super.onPreExecute()
            wakeLock.acquire(5 * 60 * 1000L /*5 minutes*/)
        }

        override fun doInBackground(vararg p0: Void?): AirQualityLog {
            return airQualityLogsRepository.getLatestLog()
        }

        override fun onPostExecute(result: AirQualityLog) {
            super.onPostExecute(result)
            notificationHelper.showAlert(result)
            wakeLock.release()
        }
    }
}