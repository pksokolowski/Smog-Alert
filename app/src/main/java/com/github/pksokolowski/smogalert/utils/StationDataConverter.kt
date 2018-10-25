package com.github.pksokolowski.smogalert.utils

import com.github.pksokolowski.smogalert.airquality.models.StationModel
import com.github.pksokolowski.smogalert.database.Station
import java.lang.Exception

/**
 * Converts data returned by API to a database's Station entity object
 */
class StationDataConverter {
    companion object {
        fun toStation(stationModel: StationModel): Station? {
            try {
                val id = stationModel.id?.toLong() ?: return null
                val latitude = stationModel.latitude?.toDouble() ?: return null
                val longitude = stationModel.longitude?.toDouble() ?: return null

                return Station(id, 0, latitude, longitude)
            } catch (e: Exception) {
                return null
            }
        }
    }
}