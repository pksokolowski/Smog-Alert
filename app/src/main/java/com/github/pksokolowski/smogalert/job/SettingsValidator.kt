package com.github.pksokolowski.smogalert.job

import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

@PerApp
class SettingsValidator @Inject constructor(private val jobsHelper: JobsHelper,
                                            private val settingsBackupHelper: SettingsBackupHelper) {

    fun validate() {
        val active = jobsHelper.getAirCheckParams().sensitivity
        val backedUp = settingsBackupHelper.getSensitivity()

        if (active != backedUp) {
            val params = AirCheckParams(backedUp)
            jobsHelper.scheduleAirQualityCheckJob(params, false)
        }
    }

}