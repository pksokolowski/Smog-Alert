package com.github.pksokolowski.smogalert

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.widget.Toast
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.job.AirCheckParams
import com.github.pksokolowski.smogalert.job.AirCheckParams.Companion.INDEX_LEVEL_UNREACHABLE
import com.github.pksokolowski.smogalert.job.JobsHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val context: Application, private val airQualityLogsRepository: AirQualityLogsRepository, private val jobsHelper: JobsHelper) : ViewModel() {
    private val airQualityInfo = MutableLiveData<AirQualityLog>()
    private val minimumWarningIndexLevel = MutableLiveData<Int>()
            .apply { value = jobsHelper.getAirCheckParams().minimumWarningIndexLevel}

    fun getAirQualityInfo() = airQualityInfo as LiveData<AirQualityLog>
    fun getWarningIndexLevel() = minimumWarningIndexLevel as LiveData<Int>

    fun checkCurrentAirQuality() {
        val task = AirQualityDataFetcher(airQualityLogsRepository, airQualityInfo)
        task.execute()
    }

    fun setMinimumWarningIndexLevel(warningLevel: Int) {
        if (warningLevel != INDEX_LEVEL_UNREACHABLE) {
            val params = AirCheckParams(warningLevel)
            if (!jobsHelper.scheduleAirQualityCheckJob(params)) {
                Toast.makeText(context, "failed to schedule the job", Toast.LENGTH_LONG).show()
            }
        } else
            jobsHelper.unScheduleAirQualityCheckJob()
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