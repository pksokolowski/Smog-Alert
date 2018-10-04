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
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.ERROR_CODE_SUCCESS
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_USED_ACTIVE_LOCATION_METHOD
import com.github.pksokolowski.smogalert.database.AirQualityLog.Companion.FLAG_USED_API
import com.github.pksokolowski.smogalert.database.AirQualityLogsDao
import com.github.pksokolowski.smogalert.database.PollutionDetails
import com.github.pksokolowski.smogalert.database.Station
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.location.LocationHelper
import com.github.pksokolowski.smogalert.utils.AirQualityLogDataConverter
import com.github.pksokolowski.smogalert.utils.InternetConnectionChecker
import com.github.pksokolowski.smogalert.utils.SensorsPresence
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
        var flags = 0
        if (!connectionChecker.isConnectionAvailable()) {
            return AirQualityLog(errorCode = ERROR_CODE_NO_INTERNET,
                    timeStamp = timeStamp, metadata = flags)
        }

        val locationResult = locationHelper.getLastLocationData()
        if(locationResult.activeMethodEmployed) flags = flags or FLAG_USED_ACTIVE_LOCATION_METHOD
        val location = locationResult.location
                ?: return AirQualityLog(errorCode = ERROR_CODE_LOCATION_MISSING,
                        timeStamp = timeStamp, metadata = flags)

        val stations = getNearestStationsIDs(location)
                ?: return AirQualityLog(errorCode = ERROR_CODE_NO_KNOWN_STATIONS,
                        timeStamp = timeStamp, metadata = flags)
        if (stations.isEmpty()) {
            return AirQualityLog(errorCode = ERROR_CODE_STATIONS_TOO_FAR_AWAY,
                    timeStamp = timeStamp, metadata = flags)
        }

        var details = PollutionDetails()
        var gainedCoverage = SensorsPresence()
        var expectedCoverage = SensorsPresence()
        flags = flags or FLAG_USED_API
        var passes = 0

        for (s in stations) {
            val sensors = if (s.sensorFlags > 0) {
                s.sensorFlags
            } else {
                stationsRepository.fetchSensorsData(s.id)?.sensorFlags ?: continue
            }

            val expectedCoverageWithSIncluded = expectedCoverage.combinedWith(sensors)
            if (expectedCoverageWithSIncluded == expectedCoverage) continue
            expectedCoverage = expectedCoverageWithSIncluded

            val log = getLogFromAPI(s.id, timeStamp)
            val logsSensorCoverage = log.details.getSensorCoverage()
            gainedCoverage = gainedCoverage.combinedWith(logsSensorCoverage)
            details = details.combinedWith(log.details)

            if (gainedCoverage.hasFullCoverage()) break
            if (++passes == MAX_STATION_REQUESTS) break
        }

        val airQualityIndex = if (gainedCoverage.hasSensors(expectedCoverage)) {
            details.getHighestIndex()
        } else {
            -1
        }

        return AirQualityLog(0,
                airQualityIndex,
                details,
                stations.first().id,
                ERROR_CODE_SUCCESS,
                timeStamp,
                flags)
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

    private fun getNearestStationsIDs(location: Location): List<Station>? {
        val stations = stationsRepository.getStations()
        if (stations.isEmpty()) return null

        class StationAndDistance(val station: Station, val distance: Float)

        val nearbyStations = mutableListOf<StationAndDistance>()

        // reusable location object
        val stationLocation = Location("station")

        stations.forEach {
            stationLocation.latitude = it.latitude
            stationLocation.longitude = it.longitude

            val distanceInMeters = location.distanceTo(stationLocation)
            if (distanceInMeters <= ACCEPTABLE_DISTANCE_TO_STATION) {
                nearbyStations.add(StationAndDistance(it, distanceInMeters))
            }
        }

        val sorted = nearbyStations.sortedWith(compareBy(StationAndDistance::distance))
        return List(sorted.size) { sorted[it].station }
    }

    private companion object {
        const val ACCEPTABLE_LOG_AGE = 30 * 60000 - 1
        const val ACCEPTABLE_DISTANCE_TO_STATION = 10000F

        const val MAX_STATION_REQUESTS = 4
    }
}