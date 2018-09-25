package com.github.pksokolowski.smogalert.repository

import android.arch.lifecycle.LiveData
import android.location.Location
import com.github.pksokolowski.smogalert.airquality.AirQualityService
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_AIR_QUALITY_MISSING
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_LOCATION_MISSING
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_NO_INTERNET
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_STATIONS_TOO_FAR_AWAY
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_NO_KNOWN_STATIONS
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_USED_API
import com.github.pksokolowski.smogalert.database.AirQualityLogsDao
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.location.LocationHelper
import com.github.pksokolowski.smogalert.utils.AirQualityLogDataConverter
import com.github.pksokolowski.smogalert.utils.InternetConnectionChecker
import java.util.*
import javax.inject.Inject

@PerApp
class AirQualityLogsRepository @Inject constructor(private val airQualityLogsDao: AirQualityLogsDao,
                                                   private val airQualityService: AirQualityService,
                                                   private val stationsRepository: StationsRepository,
                                                   private val locationHelper: LocationHelper,
                                                   private val connectionChecker: InternetConnectionChecker) {

    class LogData(val log: AirQualityLog, val isFromCache: Boolean)
    fun getLatestLogData(): LogData {
        val timeNow = Calendar.getInstance().timeInMillis
        val latestCachedLog = airQualityLogsDao.getLatestAirQualityLog()
        if (latestCachedLog == null
                || latestCachedLog.timeStamp < timeNow - ACCEPTABLE_LOG_AGE
                || !latestCachedLog.hasFlag(FLAG_USED_API)) {
            val freshLog = fetchFreshLog(timeNow)
            val logId = airQualityLogsDao.insertAirQualityLog(freshLog)
            return LogData(freshLog.assignId(logId), false)
        }
        return LogData(latestCachedLog, true)
    }

    class LogsData(val logs: List<AirQualityLog>, val isLatestFromCache: Boolean)
    fun getNLatestLogs(n: Int): LogsData {
        val isLatestFromCache = getLatestLogData().isFromCache
        return LogsData(airQualityLogsDao.getNLatestLogs(n), isLatestFromCache)
    }

    fun getCachedLog(): LiveData<AirQualityLog?> {
        return airQualityLogsDao.getLatestCachedAirQualityLog()
    }

    private fun fetchFreshLog(timeStamp: Long): AirQualityLog {
        if(!connectionChecker.isConnectionAvailable()){
            return AirQualityLog(errorCode = ERROR_CODE_NO_INTERNET,
                    timeStamp = timeStamp)
        }

        val location = locationHelper.getLastLocationData().location
                ?: return AirQualityLog(errorCode = ERROR_CODE_LOCATION_MISSING,
                        timeStamp = timeStamp)

        val stations = getNearestStationsIDs(location)
                ?: return AirQualityLog(errorCode = ERROR_CODE_NO_KNOWN_STATIONS,
                        timeStamp = timeStamp)
        if (stations.isEmpty()) {
            return AirQualityLog(errorCode = ERROR_CODE_STATIONS_TOO_FAR_AWAY,
                    timeStamp = timeStamp)
        }

        val nearest = getLogFromAPI(stations.first(), timeStamp)
        var flags = FLAG_USED_API

        // check if the nearest station is fine, otherwise consider a further station
//        if (stations.size > 1
//                && (!nearest.hasParticulateMatterData() || nearest.airQualityIndex == -1)) {
//            flags = flags or FLAG_ALSO_CHECKED_BACKUP_STATION
//            // second API request
//            val further = getLogFromAPI(stations[1], timeStamp)
//
//            if (further.airQualityIndex != -1
//                    && further.airQualityIndex > nearest.airQualityIndex
//                    && further.hasParticulateMatterData()) {
//                flags = flags or FLAG_CHOSEN_DATA_FROM_BACKUP_STATION
//                return further.addFlags(flags)
//            }
//        }

        return nearest.addFlags(flags)
    }

    private fun getLogFromAPI(stationId: Long, timeStamp: Long): AirQualityLog {
        val call = airQualityService.getCurrentAQ(stationId)
        val apiResponse = try {
            call.execute().body()
                    ?: return AirQualityLog(nearestStationId = stationId,
                            errorCode = ERROR_CODE_AIR_QUALITY_MISSING,
                            timeStamp = timeStamp)
        } catch (e: Exception) {
            return AirQualityLog(nearestStationId = stationId,
                    errorCode = ERROR_CODE_AIR_QUALITY_MISSING,
                    timeStamp = timeStamp)
        }

        return AirQualityLogDataConverter.toAirQualityLog(
                apiResponse,
                stationId,
                timeStamp)
    }

    private fun getNearestStationsIDs(location: Location): List<Long>? {
        val stations = stationsRepository.getStations()
        if (stations.isEmpty()) return null

        class StationAndDistance(val stationId: Long, val distance: Float)

        val nearbyStations = mutableListOf<StationAndDistance>()

        stations.forEach {
            val stationLocation = Location("station").apply {
                latitude = it.latitude
                longitude = it.longitude
            }
            val distanceInMeters = location.distanceTo(stationLocation)
            if (distanceInMeters <= ACCEPTABLE_DISTANCE_TO_STATION) {
                nearbyStations.add(StationAndDistance(it.id, distanceInMeters))
            }
        }

        val sorted = nearbyStations.sortedWith(compareBy(StationAndDistance::distance))
        return List(sorted.size) { sorted[it].stationId }
    }

    private companion object {
        const val ACCEPTABLE_LOG_AGE = 30 * 60000 - 1
        const val ACCEPTABLE_DISTANCE_TO_STATION = 10000F
    }
}