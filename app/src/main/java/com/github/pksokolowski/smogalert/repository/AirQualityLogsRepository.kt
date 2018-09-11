package com.github.pksokolowski.smogalert.repository

import android.location.Location
import com.github.pksokolowski.smogalert.airquality.AirQualityService
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_AIR_QUALITY_MISSING
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_LOCATION_MISSING
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_STATION_MISSING
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_SUCCESS
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.VERDICT_ACCEPTABLE
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.VERDICT_ALARMING
import com.github.pksokolowski.smogalert.database.AirQualityLogsDao
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.location.LocationHelper
import java.util.*
import javax.inject.Inject

@PerApp
class AirQualityLogsRepository @Inject constructor(private val airQualityLogsDao: AirQualityLogsDao,
                                                   private val airQualityService: AirQualityService,
                                                   private val stationsRepository: StationsRepository,
                                                   private val locationHelper: LocationHelper) {

    fun getLatestLog(): AirQualityLog {
        val timeNow = Calendar.getInstance().timeInMillis
        val latestCachedLog = airQualityLogsDao.getLatestAirQualityLog()
        if (latestCachedLog == null || latestCachedLog.timeStamp < timeNow - ACCEPTABLE_LOG_AGE) {
            val freshLog = fetchFreshLog(timeNow)
            val logId = airQualityLogsDao.insertAirQualityLog(freshLog)
            return freshLog.assignId(logId)
        }
        return latestCachedLog
    }

    private fun fetchFreshLog(timeStamp: Long): AirQualityLog {
        val location = locationHelper.getLastLocationData().location
                ?: return AirQualityLog(errorCode = ERROR_CODE_LOCATION_MISSING,
                        timeStamp = timeStamp)

        val stationId = getNearestStationID(location)
                ?: return AirQualityLog(errorCode = ERROR_CODE_STATION_MISSING,
                        timeStamp = timeStamp)

        val call = airQualityService.getCurrentAQ(stationId)
        val apiResponse = try {call.execute().body()
                ?: return AirQualityLog(stationId = stationId,
                        errorCode = ERROR_CODE_AIR_QUALITY_MISSING,
                        timeStamp = timeStamp)
        } catch (e: Exception){
            return  AirQualityLog(stationId = stationId,
                    errorCode = ERROR_CODE_AIR_QUALITY_MISSING,
                    timeStamp = timeStamp)
        }

        val airQualityIndex = apiResponse.stIndexLevel?.id ?: -1

        return AirQualityLog(0,
                generateVerdict(airQualityIndex),
                airQualityIndex,
                stationId,
                ERROR_CODE_SUCCESS,
                timeStamp)
    }

    private fun generateVerdict(rawIndex: Int): Int{
        return if(rawIndex >= 4) VERDICT_ALARMING else VERDICT_ACCEPTABLE
    }

    private fun getNearestStationID(location: Location): Long? {
        val stations = stationsRepository.getStations()
        if (stations.isEmpty()) return null

        // find the nearest station
        var minDistance = 10000000F
        var idWithMinDistance = -1L
        stations.forEach {
            val stationLocation = Location("station").apply {
                latitude = it.latitude
                longitude = it.longitude
            }

            val distanceInMeters = location.distanceTo(stationLocation)
            if (distanceInMeters < minDistance) {
                minDistance = distanceInMeters
                idWithMinDistance = it.id
            }
        }
        return idWithMinDistance
    }

    private companion object {
        const val ACCEPTABLE_LOG_AGE = 30 * 60000
    }
}