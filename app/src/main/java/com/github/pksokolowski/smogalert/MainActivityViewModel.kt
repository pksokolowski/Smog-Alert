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
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val context: Application, private val airQualityLogsRepository: AirQualityLogsRepository, private val jobsHelper: JobsHelper) : ViewModel() {
    private val airQualityInfo = MutableLiveData<AirQualityLog>()
    private val sensitivity = MutableLiveData<Int>()
            .apply { value = jobsHelper.getAirCheckParams().sensitivity }

    fun getAirQualityInfo() = airQualityInfo as LiveData<AirQualityLog>
    fun getSensitivity() = sensitivity as LiveData<Int>

    fun checkCurrentAirQuality() {
        val task = AirQualityDataFetcher(airQualityLogsRepository, airQualityInfo)
        task.execute()
    }

    fun setMinimumWarningIndexLevel(sensitivity: Int) {
        val params = AirCheckParams(sensitivity)
        if (!jobsHelper.scheduleAirQualityCheckJob(params)) {
            Toast.makeText(context, "failed to schedule the job", Toast.LENGTH_LONG).show()
        }
    }

    private class AirQualityDataFetcher(private val repo: AirQualityLogsRepository, private val outputLiveData: MutableLiveData<AirQualityLog>) : AsyncTask<Void, Void, AirQualityLog>() {

        override fun doInBackground(vararg params: Void?): AirQualityLog {
            return repo.getLatestLog()
        }

        override fun onPostExecute(result: AirQualityLog) {
            super.onPostExecute(result)
            outputLiveData.value = result
        }
    }
}