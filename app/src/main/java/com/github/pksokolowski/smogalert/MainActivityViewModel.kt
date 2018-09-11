package com.github.pksokolowski.smogalert

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val airQualityLogsRepository: AirQualityLogsRepository) : ViewModel() {
    private val airQualityInfo = MutableLiveData<AirQualityLog>()

    init {
        checkCurrentAirQuality()
    }

    fun getAirQualityInfo(): LiveData<AirQualityLog> {
        return airQualityInfo
    }

    fun checkCurrentAirQuality() {
        val task = AirQualityDataFetcher(airQualityLogsRepository, airQualityInfo)
        task.execute()
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