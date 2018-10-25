package com.github.pksokolowski.smogalert.repository

import com.github.pksokolowski.smogalert.airquality.SensorsService
import com.github.pksokolowski.smogalert.airquality.StationsService
import com.github.pksokolowski.smogalert.database.Station
import com.github.pksokolowski.smogalert.database.StationsDao
import com.github.pksokolowski.smogalert.database.StationsUpdateLog
import com.github.pksokolowski.smogalert.database.StationsUpdateLog.Companion.STATUS_FAILURE_POSTPONED
import com.github.pksokolowski.smogalert.database.StationsUpdateLog.Companion.STATUS_FAILURE_RETRY_SOON
import com.github.pksokolowski.smogalert.database.StationsUpdateLog.Companion.STATUS_SUCCESS
import com.github.pksokolowski.smogalert.database.StationsUpdateLogsDao
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.utils.SensorsDataConverter
import com.github.pksokolowski.smogalert.utils.StationDataConverter
import java.util.*
import javax.inject.Inject

@PerApp
class StationsRepository @Inject constructor(private val stationsDao: StationsDao,
                                             private val stationsService: StationsService,
                                             private val sensorsService: SensorsService,
                                             private val stationsUpdateLogsDao: StationsUpdateLogsDao) {

    fun getStations(): List<Station> {
        if (shouldUpdateCache()) {
            val updateResult = updateCache()

            if (updateResult.status == SUCCESS) {
                logCacheUpdate(STATUS_SUCCESS)
            } else {
                if (updateResult.stations.isNotEmpty()) {
                    logCacheUpdate(STATUS_FAILURE_POSTPONED)
                } else {
                    logCacheUpdate(STATUS_FAILURE_RETRY_SOON)
                }
            }
            return updateResult.stations
        }

        return stationsDao.getStations()
    }

    private fun shouldUpdateCache(): Boolean {
        val log = stationsUpdateLogsDao.getLastLog() ?: return true
        val timeNow = Calendar.getInstance().timeInMillis

        val updateInterval = when (log.status) {
            STATUS_SUCCESS -> CACHE_UPDATE_INTERVAL_AFTER_SUCCESS
            STATUS_FAILURE_POSTPONED -> CACHE_UPDATE_INTERVAL_AFTER_FAILURE
            STATUS_FAILURE_RETRY_SOON -> CACHE_UPDATE_INTERVAL_AFTER_FAILURE_AND_WITHOUT_CACHE
            else -> 0
        }

        val nextPlannedUpdateTime = log.timeStamp + updateInterval
        if (timeNow >= nextPlannedUpdateTime) return true
        return false
    }

    private fun logCacheUpdate(status: Int) {
        val timeNow = Calendar.getInstance().timeInMillis
        stationsUpdateLogsDao.insertLog(StationsUpdateLog(0, status, timeNow))
    }

    private fun updateCache(): UpdateOperationResult {
        // obtain data from sources
        val stationsFromDb = stationsDao.getStations()
        val stationsFromApi = getStationsFromApi()
                ?: return UpdateOperationResult(FAILURE, stationsFromDb)

        // if there are suspiciously many or few stations, indicate failure
        with(stationsFromApi) {
            if (size > 10000 || size == 0) return UpdateOperationResult(FAILURE, stationsFromDb)
        }

        // transform into hashMaps for quick searches
        val apiStationsMap = stationsFromApi.map { it.id to it }.toMap()
        val dbStationsMap = stationsFromDb.map { it.id to it }.toMap()

        // declare lists of items to insert, modify and delete
        val toInsert = mutableListOf<Station>()
        val toUpdate = mutableListOf<Station>()
        val toDelete = mutableListOf<Station>()

        // find new stations as well as ones to be updated
        stationsFromApi.forEach {
            val cached = dbStationsMap[it.id]
            if (cached != null) {
                if (cached != it) {
                    toUpdate.add(it)
                }
            } else {
                toInsert.add(it)
            }
        }

        // find stations to delete
        stationsFromDb.forEach {
            if (!apiStationsMap.containsKey(it.id)) {
                val incremented = it.incrementAbsenceCount()

                if (incremented.absenceCount > MAX_ABSENCE_COUNT_BEFORE_DELETION) {
                    toDelete.add(it)
                } else {
                    toUpdate.add(incremented)
                }
            }
        }

        // perform database operations
        with(stationsDao) {
            deleteStations(toDelete)
            insertStations(toInsert)
            updateStations(toUpdate)
        }

        return UpdateOperationResult(SUCCESS, stationsFromApi)
    }

    private fun getStationsFromApi(): List<Station>? {
        val stationsFromApi = try {
            stationsService.getStations().execute().body() ?: return null
        } catch (e: Exception) {
            return null
        }

        return List(stationsFromApi.size) { i ->
            StationDataConverter.toStation(stationsFromApi[i]) ?: return null
        }
    }

    fun fetchSensorsData(stationId: Long): Station? {
        val cachedVersion = stationsDao.getStationById(stationId) ?: return null
        if (cachedVersion.sensorFlags == 0) {
            val sensors = try {
                sensorsService.getSensors(stationId).execute().body() ?: return null
            } catch (e: Exception) {
                return null
            }

            val sensorsFlags = SensorsDataConverter.toSensorFlags(sensors)
            // was 0 and remains 0, nothing changed so don't save and don't return as if
            // you had some results.
            if (sensorsFlags == 0) return null

            val updatedStation = cachedVersion.assignSensors(sensorsFlags)
            stationsDao.updateStation(updatedStation)

            return updatedStation
        }
        return cachedVersion
    }

    private companion object {
        const val SUCCESS = 0
        const val FAILURE = 1
        const val DAY_IN_MILLIS = 86400000L
        const val CACHE_UPDATE_INTERVAL_AFTER_FAILURE = DAY_IN_MILLIS
        const val CACHE_UPDATE_INTERVAL_AFTER_FAILURE_AND_WITHOUT_CACHE = 30 * 60000L - 1
        const val CACHE_UPDATE_INTERVAL_AFTER_SUCCESS = DAY_IN_MILLIS
        const val MAX_ABSENCE_COUNT_BEFORE_DELETION = 6
    }

    private class UpdateOperationResult(val status: Int, val stations: List<Station>)
}