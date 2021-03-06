package com.github.pksokolowski.smogalert.job

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.AsyncTask
import android.os.PowerManager
import com.github.pksokolowski.smogalert.db.AirQualityLog.Companion.FLAG_BACKGROUND_REQUEST
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_BAD_AFTER_SHORTAGE_ENDED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DATA_SHORTAGE_STARTED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_DEGRADED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_ERROR_EMERGED
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_IMPROVED_PAST_THRESHOLD
import com.github.pksokolowski.smogalert.job.AQLogsComparer.Companion.RESULT_LIKELY_OK
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
        // does not cancel the asyncTask on purpose. If it started, it is supposed to finish,
        // so the app handles connection issues better and intelligently retries if needed within
        // a single logic, rather than with a separate solution just for the jobScheduler.
        return false
    }

    override fun onStartJob(jobParams: JobParameters): Boolean {
        val checkParams = AirCheckParams(jobParams.extras)

        class AirQualityCheckerTask(private val airQualityLogsRepository: AirQualityLogsRepository,
                                    private val notificationHelper: NotificationHelper)
            : AsyncTask<Void, Void, LogsData>() {

            // an explicit wakelock is used, instead of the jobScheduler's one, in order not to
            // stop the execution if preferred conditions are no longer met for it.
            // the null safety was introduced mainly to prevent tests from breaking.
            val wakeLock: PowerManager.WakeLock? =
                    (getSystemService(Context.POWER_SERVICE) as? PowerManager)?.run {
                        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.github.pksokolowski.smogalert::AirQualityUpdate")
                    }

            override fun onPreExecute() {
                super.onPreExecute()
                wakeLock?.setReferenceCounted(false)
                wakeLock?.acquire(70000)
            }

            override fun doInBackground(vararg p0: Void?): LogsData {
                return airQualityLogsRepository.getNLatestLogs(3, FLAG_BACKGROUND_REQUEST)
            }

            override fun onPostExecute(data: LogsData) {
                super.onPostExecute(data)

                // if the latest log comes from cache, it is assumed that the user
                // had already interacted with it, and it's old news by now.
                if (data.isLatestFromCache) {
                    wakeLock?.release()
                    return
                }

                val current = data.logs.getOrNull(0)
                val comparisonPoint = if (checkParams.isOneTimeRetry) data.logs.getOrNull(2)
                else data.logs.getOrNull(1)

                val warningIndexLevel = checkParams.getMinimumWarningIndexLevel()
                val comparisonResult = AQLogsComparer.compare(current, comparisonPoint, warningIndexLevel)

                when (comparisonResult) {
                    RESULT_DEGRADED_PAST_THRESHOLD -> notificationHelper.showAlert()
                    RESULT_IMPROVED_PAST_THRESHOLD -> notificationHelper.showImprovement()
                    RESULT_LIKELY_OK -> notificationHelper.showLikelyOk()
                    RESULT_OK_AFTER_SHORTAGE_ENDED -> notificationHelper.showAirIsOkAfterShortage()
                    RESULT_BAD_AFTER_SHORTAGE_ENDED -> notificationHelper.showBadAfterShortage()
                    RESULT_DATA_SHORTAGE_STARTED -> notificationHelper.showDataShortage()
                    RESULT_ERROR_EMERGED ->
                        if (checkParams.isOneTimeRetry) {
                            notificationHelper.showError()
                        } else {
                            jobsHelper.scheduleOneTimeRetry(checkParams.sensitivity)
                        }
                    else -> {
                    }
                }

                wakeLock?.release()
            }
        }
        task = AirQualityCheckerTask(airQualityLogsRepository, notificationHelper)

        task?.execute()
        return false
    }

}