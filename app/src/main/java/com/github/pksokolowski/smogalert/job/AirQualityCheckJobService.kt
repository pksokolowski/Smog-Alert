package com.github.pksokolowski.smogalert.job

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.AsyncTask
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.notifications.NotificationHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import dagger.android.AndroidInjection
import javax.inject.Inject

class AirQualityCheckJobService : JobService() {

    @Inject
    lateinit var airQualityLogsRepository: AirQualityLogsRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    var task: AsyncTask<Void, Void, AirQualityLog>? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStopJob(jobParams: JobParameters?): Boolean {
        task?.cancel(true)
        return false
    }

    override fun onStartJob(jobParams: JobParameters?): Boolean {
        class AirQualityCheckerTask(private val airQualityLogsRepository: AirQualityLogsRepository,
                                    private val notificationHelper: NotificationHelper)
            : AsyncTask<Void, Void, AirQualityLog>() {

            override fun doInBackground(vararg p0: Void?): AirQualityLog {
                return airQualityLogsRepository.getLatestLog()
            }

            override fun onPostExecute(result: AirQualityLog) {
                super.onPostExecute(result)
                notificationHelper.showAlert(result)
                jobFinished(jobParams, false)
            }
        }
        task = AirQualityCheckerTask(airQualityLogsRepository, notificationHelper)

        task?.execute()
        return false
    }


}