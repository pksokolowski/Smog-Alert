package com.github.pksokolowski.smogalert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.job.AirCheckParams
import com.github.pksokolowski.smogalert.job.JobsHelper
import com.github.pksokolowski.smogalert.job.SettingsValidator
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository.LogData
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.junit.rules.TestRule
import org.junit.Rule

@RunWith(MockitoJUnitRunner::class)
class MainActivityViewModelTest {

    @Test
    fun propagatesIndexFromCache() {
        val expected = AirQualityLog(1, 2, PollutionDetails(9999992), 550, 0, 1, 1)
        var returned: AirQualityLog? = null

        setup(cachedLog = expected)
        viewModel.getAirQualityInfo().observeForever { returned = it }
        assertEquals(expected, returned)
    }

    @Test
    fun downloadsDataFromAPIWhenRequested() {
        val expected = AirQualityLog(2, 2, PollutionDetails(9999992), 550, 0, 2, 1)
        var returned: AirQualityLog? = null

        setup(nextLogData = LogData(expected, false))
        viewModel.getAirQualityInfo().observeForever { returned = it }

        viewModel.checkCurrentAirQuality()
        assertEquals(expected, returned)
    }

    @Test
    fun downloadStatusIsChangingAsExpected() {
        setup()

        // there is no additional false at the beginning
        // because the liveData is not having any value set
        // at the viewModel object's creation.
        val expectedSequence = listOf(true, false)
        val actualSequence = mutableListOf<Boolean>()

        viewModel.getDownloadStatus().observeForever {
            if (it != null) actualSequence.add(it)
        }
        viewModel.checkCurrentAirQuality()

        assertEquals(expectedSequence, actualSequence)
    }

    @Test
    fun returnsAirCheckParams() {
        setup(airCheckParams = AirCheckParams(2, false))
        var sensitivity = -1
        viewModel.getSensitivity().observeForever { sensitivity = it ?: -1 }
        assertEquals(2, sensitivity)
    }

    @Test
    fun settingAirCheckParamsWorks() {
        setup(airCheckParams = AirCheckParams(2, false))
        var sensitivity = -1
        viewModel.getSensitivity().observeForever { sensitivity = it ?: -1 }
        viewModel.setSensitivity(3)
        assertEquals(3, sensitivity)
    }

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainActivityViewModel

    @Mock
    private lateinit var mockJobsHelper: JobsHelper

    @Mock
    private lateinit var mockAirQualityLogsRepository: AirQualityLogsRepository

    @Mock
    private lateinit var mockSettingsValidator: SettingsValidator

    private val cacheLiveData = MutableLiveData<AirQualityLog?>()

    companion object {
        val defaultLog = AirQualityLog(1, 1, PollutionDetails(1111111), 550, 0, 1, 1)
    }

    /** This method allows for clear and concise parameterised setup of the instance of the class under test.
     *
     * It provides meaningful default values, so only the ones of interest have to be set manually in each test.
     * However, it's important to exercise great caution when modifying those defaults, as test may rely on them heavily.
     *
     * @param cachedLog either a log or null to be present in cache from the beginning. see defaultLog value in companion object.
     * @param nextLogData LogData which is going to be returned if the viewModel requests a fresh log download.
     * @param airCheckParams the settings, predominantly the sensitivity setting fetched from this object.
     */
    private fun setup(cachedLog: AirQualityLog? = defaultLog,
                      nextLogData: LogData = LogData(cachedLog ?: defaultLog, false),
                      airCheckParams: AirCheckParams = AirCheckParams(4, false)) {

        fun downloadNewLog(): LogData {
            cacheLiveData.value = nextLogData.log
            return nextLogData
        }
        cacheLiveData.value = cachedLog
        `when`(mockAirQualityLogsRepository.getCachedLog()).thenReturn(cacheLiveData)
        `when`(mockAirQualityLogsRepository.getLatestLogData()).thenAnswer { downloadNewLog() }
        `when`(mockJobsHelper.getAirCheckParams()).thenReturn(airCheckParams)
        viewModel = MainActivityViewModel(mockAirQualityLogsRepository, mockJobsHelper, mockSettingsValidator)
    }
}
