package com.github.pksokolowski.smogalert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.pksokolowski.smogalert.job.AirCheckParams
import com.github.pksokolowski.smogalert.job.JobsHelper
import com.github.pksokolowski.smogalert.job.SettingsValidator
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val airQualityLogsRepository: AirQualityLogsRepository,
                                                private val jobsHelper: JobsHelper,
                                                settingsValidator: SettingsValidator) : ViewModel() {

    init {
        settingsValidator.validate()
    }

    private val airQualityInfo = airQualityLogsRepository.getCachedLog()
    private val isDownloadInProgress = MutableLiveData<Boolean>()
    private val sensitivity = MutableLiveData<Int>()
            .apply { value = jobsHelper.getAirCheckParams().sensitivity }

    fun getAirQualityInfo() = airQualityInfo
    fun getDownloadStatus() = isDownloadInProgress as LiveData<Boolean>
    fun getSensitivity() = sensitivity as LiveData<Int>

    fun checkCurrentAirQuality() = GlobalScope.launch(Dispatchers.Main) {
        isDownloadInProgress.value = true
        withContext(Dispatchers.Default) {
            val result = airQualityLogsRepository.getLatestLogData()
            if (!result.isFromCache) jobsHelper.reschedule()
        }
        isDownloadInProgress.value = false
    }

    fun setSensitivity(sensitivity: Int) {
        val params = AirCheckParams(sensitivity)
        jobsHelper.scheduleAirQualityCheckJob(params)
        this.sensitivity.value = sensitivity
    }

}