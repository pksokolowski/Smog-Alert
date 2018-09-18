package com.github.pksokolowski.smogalert.job

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.AsyncTask
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DATA_SHORTAGE_STARTED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DEGRADED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_ERROR_EMERGED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_IMPROVED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.notifications.NotificationHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import dagger.android.AndroidInjection
import javax.inject.Inject

class AirQualityCheckJobService : JobService() {

    @Inject
    lateinit var airQualityLogsRepository: AirQualityLogsRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    var task: AsyncTask<Void, Void, List<AirQualityLog>>? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStopJob(jobParams: JobParameters?): Boolean {
        task?.cancel(true)
        return false
    }

    override fun onStartJob(jobParams: JobParameters): Boolean {
        val checkParams = AirCheckParams(jobParams.extras)

        class AirQualityCheckerTask(private val airQualityLogsRepository: AirQualityLogsRepository,
                                    private val notificationHelper: NotificationHelper)
            : AsyncTask<Void, Void, List<AirQualityLog>>() {

            override fun doInBackground(vararg p0: Void?): List<AirQualityLog> {
                return airQualityLogsRepository.getNLatestLogs(2)
            }

            override fun onPostExecute(results: List<AirQualityLog>) {
                super.onPostExecute(results)
                val current = results.getOrNull(0)
                if (current == null) {
                    jobFinished(jobParams, false)
                    return
                }
                val previous = results.getOrNull(1)
                val comparisonResult = AQLogsComparer.compare(current,
                        previous,
                        checkParams.getMinimumWarningIndexLevel())

                when (comparisonResult) {
                    RESULT_DEGRADED_PAST_THRESHOLD -> notificationHelper.showAlert()
                    RESULT_IMPROVED_PAST_THRESHOLD -> notificationHelper.showImprovement()
                    RESULT_DATA_SHORTAGE_STARTED -> notificationHelper.showDataShortage()
                    RESULT_ERROR_EMERGED -> notificationHelper.showError()
                    else -> {
                    }
                }

                jobFinished(jobParams, false)
            }
        }
        task = AirQualityCheckerTask(airQualityLogsRepository, notificationHelper)

        task?.execute() ?: return true
        return false
    }


}