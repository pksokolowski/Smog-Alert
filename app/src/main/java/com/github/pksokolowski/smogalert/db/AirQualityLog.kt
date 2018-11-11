package com.github.pksokolowski.smogalert.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.github.pksokolowski.smogalert.utils.SensorsPresence

/*
 * Notice that nearestStationId is not marked as a foreign key here. This is intentional, done to prevent
 * overhead of maintaining an index on each insert operation. Rationale is presented below:
 *
 * 1. Whatever happens to the parent table (stations) this table is meant to remain unchanged.
 * Therefore it appears that there is no need for the index, which would improve searches of affected
 * rows in this table.
 * 2. Searching logs by station id is unlikely in this app, with it's very philosophy of being as
 * simple as humanly possible.
 * 3. Overhead of additional log(n) index maintenance per insert is maybe small but not in exchange
 * for nothing.
 * 4. Should there arise any need for the performance boost from the index, a simple migration can
 * introduce it immediately. As for now, the nearestStationId column is only included just in case,
 * because it seems likely, that at some point such information can potentially be of use.
 */
@Entity(tableName = "air_quality_logs")
data class AirQualityLog(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long = 0,

        @ColumnInfo(name = "air_quality_index")
        val airQualityIndex: Int = -1,

        @ColumnInfo(name = "air_quality_details")
        val details: PollutionDetails = PollutionDetails(),

        @ColumnInfo(name = "nearest_station_id")
        val nearestStationId: Long = -1,

        @ColumnInfo(name = "error_code")
        val errorCode: Int = 0,

        @ColumnInfo(name = "time_stamp")
        val timeStamp: Long,

        @ColumnInfo(name = "metadata")
        val metadata: Int = 0,

        @ColumnInfo(name = "expected_sensor_coverage")
        val expectedSensorCoverage: SensorsPresence = SensorsPresence(0)
) {
    fun assignId(id: Long) =
            AirQualityLog(id, airQualityIndex, details,  nearestStationId, errorCode, timeStamp, metadata, expectedSensorCoverage)

    fun hasFlags(flags: Int) = metadata and flags == flags

    fun hasExpectedCoverage() = details.getSensorCoverage().hasSensors(expectedSensorCoverage) && errorCode == 0

    fun hasIndex() = airQualityIndex != -1

    companion object {
        const val ERROR_CODE_SUCCESS = 0
        const val ERROR_CODE_NO_INTERNET = 1
        const val ERROR_CODE_LOCATION_MISSING = 2
        const val ERROR_CODE_NO_KNOWN_STATIONS = 3
        const val ERROR_CODE_STATIONS_TOO_FAR_AWAY = 4
        const val ERROR_CODE_AIR_QUALITY_MISSING = 5

        const val FLAG_USED_API = 1
        const val FLAG_USED_ACTIVE_LOCATION_METHOD = 2
    }

}