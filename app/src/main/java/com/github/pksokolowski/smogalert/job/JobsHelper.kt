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

    fun scheduleAirQualityCheckJob(airCheckParams: AirCheckParams): Boolean {
        if(airCheckParams.isOneTimeRetry){
            return scheduleOneTimeRetry(airCheckParams)
        }
        jobScheduler.cancel(RETRY_JOB_ID)

        if (airCheckParams.sensitivity == 0) {
            jobScheduler.cancel(PERIODIC_JOB_ID)
            return true
        }

        val jobInfo = JobInfo.Builder(PERIODIC_JOB_ID, ComponentName(context, AirQualityCheckJobService::class.java))
                .setPeriodic(PERIOD, FLEX)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(airCheckParams.getExtras())
                .build()

        val result = jobScheduler.schedule(jobInfo)
        return result == RESULT_SUCCESS
    }

    private fun scheduleOneTimeRetry(airCheckParams: AirCheckParams): Boolean{
        val jobInfo = JobInfo.Builder(RETRY_JOB_ID, ComponentName(context, AirQualityCheckJobService::class.java))
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(MIN_EXECUTION_DELAY_FOR_RETRY_JOB)
                .setOverrideDeadline(MAX_EXECUTION_DELAY_FOR_RETRY_JOB)
                .setExtras(airCheckParams.getExtras())
                .build()

        val result = jobScheduler.schedule(jobInfo)
        return result == RESULT_SUCCESS
    }

    fun scheduleOneTimeRetry(sensitivity: Int): Boolean{
        val retryParams = AirCheckParams(sensitivity, true)
        return scheduleOneTimeRetry(retryParams)
    }

    fun getAirCheckParams(): AirCheckParams {
        val job = jobScheduler.getPendingJob(PERIODIC_JOB_ID)
                ?: return AirCheckParams(0)

        return AirCheckParams(job.extras)
    }

    fun reschedule() {
        val params = getAirCheckParams()
        if (params.sensitivity == 0) return
        scheduleAirQualityCheckJob(params)
    }

    private companion object {
        const val PERIODIC_JOB_ID = 0
        const val RETRY_JOB_ID = 1

        const val PERIOD = 60 * 60000L /* 1 hour */
        const val FLEX = 30 * 60000L /* 30 minutes */

        const val MIN_EXECUTION_DELAY_FOR_RETRY_JOB = 11 * 60000L /* 11 minutes */
        const val MAX_EXECUTION_DELAY_FOR_RETRY_JOB = 19 * 60000L /* 19 minutes */
    }
}