package com.github.pksokolowski.smogalert

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.widget.Toast
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.job.AirCheckParams
import com.github.pksokolowski.smogalert.job.JobsHelper
import com.github.pksokolowski.smogalert.location.LocationAvailabilityHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val context: Application,
                                                private val airQualityLogsRepository: AirQualityLogsRepository,
                                                private val jobsHelper: JobsHelper,
                                                private val locationAvailabilityHelper: LocationAvailabilityHelper) : ViewModel() {

    private val airQualityInfo = airQualityLogsRepository.getCachedLog()
    private val isDownloadInProgress = MutableLiveData<Boolean>()
    private val sensitivity = MutableLiveData<Int>()
            .apply { value = jobsHelper.getAirCheckParams().sensitivity }

    fun getAirQualityInfo() = airQualityInfo
    fun getDownloadStatus() = isDownloadInProgress as LiveData<Boolean>
    fun getSensitivity() = sensitivity as LiveData<Int>

    fun checkCurrentAirQuality() {
        if (!locationAvailabilityHelper.checkOverallAvailability()) return

        val task = AirQualityDataFetcher(airQualityLogsRepository, isDownloadInProgress)
        task.execute()
    }

    fun setSensitivity(sensitivity: Int) {
        val params = AirCheckParams(sensitivity)
        if (!jobsHelper.scheduleAirQualityCheckJob(params)) {
            Toast.makeText(context, "failed to schedule the job", Toast.LENGTH_LONG).show()
        }
        this.sensitivity.value = sensitivity
        checkCurrentAirQuality()
    }

    private class AirQualityDataFetcher(private val repo: AirQualityLogsRepository,
                                        private val isDownloadInProgress: MutableLiveData<Boolean>) : AsyncTask<Void, Void, AirQualityLog>() {

        override fun onPreExecute() {
            super.onPreExecute()
            isDownloadInProgress.value = true
        }

        override fun doInBackground(vararg params: Void?): AirQualityLog {
            return repo.getLatestLog()
        }

        override fun onPostExecute(result: AirQualityLog) {
            super.onPostExecute(result)
            isDownloadInProgress.value = false
        }
    }

}