package com.github.pksokolowski.smogalert

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import com.github.pksokolowski.smogalert.job.AirCheckParams
import com.github.pksokolowski.smogalert.job.JobsHelper
import com.github.pksokolowski.smogalert.job.SettingsValidator
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository.LogData
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

    fun checkCurrentAirQuality() {
        val task = AirQualityDataFetcher(airQualityLogsRepository, jobsHelper, isDownloadInProgress)
        task.execute()
    }

    fun setSensitivity(sensitivity: Int) {
        val params = AirCheckParams(sensitivity)
        jobsHelper.scheduleAirQualityCheckJob(params)
        this.sensitivity.value = sensitivity
        checkCurrentAirQuality()
    }

    private class AirQualityDataFetcher(private val repo: AirQualityLogsRepository,
                                        private val jobsHelper: JobsHelper,
                                        private val isDownloadInProgress: MutableLiveData<Boolean>) : AsyncTask<Void, Void, LogData>() {

        override fun onPreExecute() {
            super.onPreExecute()
            isDownloadInProgress.value = true
        }

        override fun doInBackground(vararg params: Void?): LogData {
            return repo.getLatestLogData()
        }

        override fun onPostExecute(result: LogData) {
            super.onPostExecute(result)
            if (!result.isFromCache) jobsHelper.reschedule()
            isDownloadInProgress.value = false
        }
    }

}