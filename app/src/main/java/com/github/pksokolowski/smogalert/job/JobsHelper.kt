package com.github.pksokolowski.smogalert.job

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobScheduler.RESULT_SUCCESS
import android.content.ComponentName
import android.content.Context
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

@PerApp
class JobsHelper @Inject constructor(private val context: Application) {
    private val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    fun scheduleAirQualityCheckJob(): Boolean {
        val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, AirQualityCheckJobService::class.java))
                .setPeriodic(PERIOD, FLEX)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build()

        val result = jobScheduler.schedule(jobInfo)
        return result == RESULT_SUCCESS
    }

    fun unScheduleAirQualityCheckJob() {
        jobScheduler.cancel(JOB_ID)
    }

    private companion object {
        const val JOB_ID = 0

        const val PERIOD = 60 * 60000L /* 1 hour */
        const val FLEX = 30 * 60000L /* 30 minutes */
    }
}