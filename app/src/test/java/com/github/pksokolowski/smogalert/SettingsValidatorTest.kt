package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.job.AirCheckParams
import com.github.pksokolowski.smogalert.job.JobsHelper
import com.github.pksokolowski.smogalert.job.SettingsBackupHelper
import com.github.pksokolowski.smogalert.job.SettingsValidator
import com.github.pksokolowski.smogalert.utils.anything
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SettingsValidatorTest {

    @Test
    fun changesNothingIfEverythingIsCorrect() {
        setup(3, 3)
        validator.validate()
        verify(jobsHelper, never()).scheduleAirQualityCheckJob(anything(), anyBoolean())
        verify(settingsBackupHelper, never()).saveSensitivity(anyInt())
    }

    @Test
    fun changesActiveSettingIfWrong() {
        setup(0, 2)
        validator.validate()
        verify(jobsHelper).scheduleAirQualityCheckJob(AirCheckParams(2, false), false)
    }

    @Mock
    private lateinit var jobsHelper: JobsHelper

    @Mock
    private lateinit var settingsBackupHelper: SettingsBackupHelper

    private lateinit var validator: SettingsValidator

    private fun setup(active: Int, backedUp: Int) {
        `when`(jobsHelper.getAirCheckParams()).thenReturn(AirCheckParams(active, false))
        `when`(settingsBackupHelper.getSensitivity()).thenReturn(backedUp)
        validator = SettingsValidator(jobsHelper, settingsBackupHelper)
    }
}