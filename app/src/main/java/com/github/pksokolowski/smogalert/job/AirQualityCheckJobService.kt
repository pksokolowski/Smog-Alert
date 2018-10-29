package com.github.pksokolowski.smogalert.job

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.AsyncTask
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DATA_SHORTAGE_STARTED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DEGRADED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_ERROR_EMERGED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_IMPROVED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_OK_AFTER_SHORTAGE_ENDED
import com.github.pksokolowski.smogalert.notifications.NotificationHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository.LogsData
import dagger.android.AndroidInjection
import javax.inject.Inject

class AirQualityCheckJobService : JobService() {

    @Inject
    lateinit var airQualityLogsRepository: AirQualityLogsRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var jobsHelper: JobsHelper

    private var task: AsyncTask<Void, Void, LogsData>? = null

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
            : AsyncTask<Void, Void, LogsData>() {

            override fun doInBackground(vararg p0: Void?): LogsData {
                return airQualityLogsRepository.getNLatestLogs(3)
            }

            override fun onPostExecute(data: LogsData) {
                super.onPostExecute(data)

                // if the latest log comes from cache, it is assumed that the user
                // had already interacted with it, and it's old news by now.
                if (data.isLatestFromCache) {
                    jobFinished(jobParams, false)
                    return
                }

                val current = data.logs.getOrNull(0)
                val previous = data.logs.getOrNull(1)

                val warningIndexLevel = checkParams.getMinimumWarningIndexLevel()

                if (checkParams.isOneTimeRetry) {
                    // do not reschedule the periodic job here, it would cancel this job immediately

                    val third = data.logs.getOrNull(2)
                    val comparisonResult = AQLogsComparer.compare(current, third, warningIndexLevel)
                    when (comparisonResult) {
                        RESULT_DEGRADED_PAST_THRESHOLD -> notificationHelper.showAlert()
                        RESULT_IMPROVED_PAST_THRESHOLD -> notificationHelper.showImprovement()
                        RESULT_OK_AFTER_SHORTAGE_ENDED -> notificationHelper.showAirIsOkAfterShortage()
                        RESULT_DATA_SHORTAGE_STARTED -> notificationHelper.showDataShortage()
                        RESULT_ERROR_EMERGED -> notificationHelper.showError()
                        else -> {
                        }
                    }
                } else {
                    val comparisonResult = AQLogsComparer.compare(current, previous, warningIndexLevel)
                    when (comparisonResult) {
                        RESULT_DEGRADED_PAST_THRESHOLD -> notificationHelper.showAlert()
                        RESULT_IMPROVED_PAST_THRESHOLD -> notificationHelper.showImprovement()
                        RESULT_OK_AFTER_SHORTAGE_ENDED -> notificationHelper.showAirIsOkAfterShortage()
                        RESULT_DATA_SHORTAGE_STARTED -> notificationHelper.showDataShortage()
                        RESULT_ERROR_EMERGED -> jobsHelper.scheduleOneTimeRetry(checkParams.sensitivity)
                        else -> {
                        }
                    }
                }

                jobFinished(jobParams, false)
            }
        }
        task = AirQualityCheckerTask(airQualityLogsRepository, notificationHelper)

        task?.execute() ?: return false
        return true
    }


}