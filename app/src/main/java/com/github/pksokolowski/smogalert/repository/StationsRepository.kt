package com.github.pksokolowski.smogalert.repository

import com.github.pksokolowski.smogalert.airquality.StationsService
import com.github.pksokolowski.smogalert.database.Station
import com.github.pksokolowski.smogalert.database.StationsDao
import com.github.pksokolowski.smogalert.di.PerApp
import com.github.pksokolowski.smogalert.utils.ICacheMetadataHelper
import com.github.pksokolowski.smogalert.utils.StationDataConverter
import java.util.*
import javax.inject.Inject

@PerApp
class StationsRepository @Inject constructor(private val stationsDao: StationsDao, private val stationsService: StationsService, private val metadataHelper: ICacheMetadataHelper) {

    fun getStations(): List<Station> {
        val timeNow = Calendar.getInstance().timeInMillis
        val plannedUpdateTime = metadataHelper.getPlannedCacheUpdateTime()

        if (timeNow >= plannedUpdateTime) {
            val updateResult = updateCache()

            return if (updateResult.status == SUCCESS) {
                metadataHelper.setNextUpdateTime(timeNow + CACHE_UPDATE_INTERVAL_AFTER_SUCCESS)
                updateResult.stations
            } else {
                val updateInterval = if (updateResult.stations.isNotEmpty()) CACHE_UPDATE_INTERVAL_AFTER_FAILURE
                else CACHE_UPDATE_INTERVAL_AFTER_FAILURE_AND_WITHOUT_CACHE

                with(metadataHelper) {
                    incrementFailedUpdatesCount()
                    setNextUpdateTime(timeNow + updateInterval)
                }
                updateResult.stations
            }
        }

        return stationsDao.getStations()
    }

    private fun updateCache(): UpdateOperationResult {
        // obtain data from sources
        val stationsFromDb = stationsDao.getStations()
        val stationsFromApi = getStationsFromApi()
                ?: return UpdateOperationResult(FAILURE, stationsFromDb)

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
                toDelete.add(it)
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

    private companion object {
        const val SUCCESS = 0
        const val FAILURE = 1
        private const val DAY_IN_MILLIS = 86400000L
        const val CACHE_UPDATE_INTERVAL_AFTER_FAILURE = 3 * DAY_IN_MILLIS
        const val CACHE_UPDATE_INTERVAL_AFTER_FAILURE_AND_WITHOUT_CACHE = 30 * 60000L - 1
        const val CACHE_UPDATE_INTERVAL_AFTER_SUCCESS = 30 * DAY_IN_MILLIS
    }

    private class UpdateOperationResult(val status: Int, val stations: List<Station>)
}