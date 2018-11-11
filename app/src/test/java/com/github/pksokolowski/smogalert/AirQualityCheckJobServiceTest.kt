package com.github.pksokolowski.smogalert

import android.app.job.JobParameters
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.job.AirCheckParams
import com.github.pksokolowski.smogalert.job.AirQualityCheckJobService
import com.github.pksokolowski.smogalert.job.JobsHelper
import com.github.pksokolowski.smogalert.notifications.NotificationHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository.LogsData
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AirQualityCheckJobServiceTest {

    @Test
    fun doesScheduleRetryOnErrorWithNoApiUsedFlag() {
        current = AirQualityLog(3, -1, PollutionDetails(9999999), -1, 2, 3, 0)
        previous = AirQualityLog(2, 1, PollutionDetails(9199999), 123, 0, 2, 1)
        third = AirQualityLog(1, 1, PollutionDetails(9199999), 123, 0, 1, 1)
        airCheckParams = AirCheckParams(1, false)

        jobService.onStartJob(mockJobParameters)
        verify(mockJobsHelper).scheduleOneTimeRetry(airCheckParams.sensitivity)
    }

    @Test
    fun doesNotScheduleRetryWhenNothingChanges() {
        current = AirQualityLog(3, 1, PollutionDetails(9199999), 123, 0, 3, 0)
        previous = AirQualityLog(2, 1, PollutionDetails(9199999), 123, 0, 2, 1)
        third = AirQualityLog(1, 1, PollutionDetails(9199999), 123, 0, 1, 1)
        airCheckParams = AirCheckParams(1, false)

        jobService.onStartJob(mockJobParameters)
        verify(mockJobsHelper, never()).scheduleOneTimeRetry(anyInt())
    }

    @Test
    fun doesNotScheduleRetryWhenExecutingARetry() {
        current = AirQualityLog(3, 1, PollutionDetails(9999999), -1, 2, 3, 1)
        previous = AirQualityLog(2, -1, PollutionDetails(9999999), -1, 2, 2, 0)
        third = AirQualityLog(1, 1, PollutionDetails(9199999), 123, 0, 1, 1)
        airCheckParams = AirCheckParams(1, true)

        jobService.onStartJob(mockJobParameters)
        verify(mockJobsHelper, never()).scheduleOneTimeRetry(anyInt())
    }

    @Test
    fun showsNotificationWhenAirQualityDeclines() {
        current = AirQualityLog(2, 4, PollutionDetails(1411111), 100, 0, 2, 1)
        previous = AirQualityLog(1, 1, PollutionDetails(1111111), 100, 0, 1, 1)
        airCheckParams = AirCheckParams(1, false)

        jobService.onStartJob(mockJobParameters)
        verify(mockNotificationHelper).showAlert()
    }

    @Test
    fun doesNotShowAnyNotificationWhenAirStaysGood() {
        current = AirQualityLog(2, 3, PollutionDetails(1311111), 100, 0, 2, 1)
        previous = AirQualityLog(1, 1, PollutionDetails(1111111), 100, 0, 1, 1)
        airCheckParams = AirCheckParams(1, false)

        jobService.onStartJob(mockJobParameters)
        verifyZeroInteractions(mockNotificationHelper)
    }

    @Test
    fun showsErrorWhenFirstTwoLogsEverAreErrors() {
        current = AirQualityLog(2, -1, PollutionDetails(9999999), 110, 1, 2, 0)
        previous = AirQualityLog(1, -1, PollutionDetails(9999999), 110, 1, 1, 0)
        airCheckParams = AirCheckParams(1, true)

        jobService.onStartJob(mockJobParameters)
        verify(mockNotificationHelper).showError()
    }

    @Test
    fun showsLikelyOkMessageWhenSomeSensorsAreMissingButKeyOnesIndicateImprovement() {
        current = AirQualityLog(2, 1, PollutionDetails(1199999), 110, 0, 2, 1, SensorsPresence(127))
        previous = AirQualityLog(1, 2, PollutionDetails(2299999), 110, 0, 1, 1, SensorsPresence(127))
        airCheckParams = AirCheckParams(3, false)

        jobService.onStartJob(mockJobParameters)
        verify(mockNotificationHelper).showLikelyOk()
    }

    @Spy
    private lateinit var jobService: AirQualityCheckJobService

    @Mock
    private lateinit var mockAirQualityLogsRepository: AirQualityLogsRepository

    @Mock
    private lateinit var mockJobsHelper: JobsHelper

    @Mock
    private lateinit var mockNotificationHelper: NotificationHelper

    @Mock
    private lateinit var mockJobParameters: JobParameters

    // these are to be modified, they mirror variables in the tested class
    private var current: AirQualityLog? = null
    private var previous: AirQualityLog? = null
    private var third: AirQualityLog? = null

    private val isLatestFromCache = false
    private lateinit var airCheckParams: AirCheckParams

    @Before
    fun setup() {
        //doNothing().`when`(jobService).onCreate()
        `when`(jobService.jobFinished(any(), ArgumentMatchers.anyBoolean())).then { }
        jobService.airQualityLogsRepository = mockAirQualityLogsRepository
        jobService.jobsHelper = mockJobsHelper
        jobService.notificationHelper = mockNotificationHelper

        `when`(mockAirQualityLogsRepository.getNLatestLogs(3)).then {
            val logs = listOf(current, previous, third)
            val list = mutableListOf<AirQualityLog>()
            for (log in logs) {
                if (log == null) break
                list.add(log)
            }
            LogsData(list, isLatestFromCache)
        }
        `when`(mockJobParameters.extras).then { airCheckParams.getExtras() }
    }
}