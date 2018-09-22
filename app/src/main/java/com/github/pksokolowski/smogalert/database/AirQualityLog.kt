package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/*
 * Notice that stationId is not marked as a foreign key here. This is intentional, done to prevent
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
 * introduce it immediately. As for now, the stationId column is only included just in case,
 * because it seems likely, that at some point such information can potentially be of use.
 */
@Entity(tableName = "air_quality_logs")
data class AirQualityLog(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long = 0,

        @ColumnInfo(name = "air_quality_index")
        val airQualityIndex: Int = -1,

        @ColumnInfo(name = "station_id")
        val stationId: Long = -1,

        @ColumnInfo(name = "error_code")
        val errorCode: Int = 0,

        @ColumnInfo(name = "time_stamp")
        val timeStamp: Long,

        @ColumnInfo(name = "metadata")
        val metadata: Int = 0

) {
    fun assignId(id: Long) =
            AirQualityLog(id, airQualityIndex, stationId, errorCode, timeStamp, metadata)

    fun hasSensor(sensorFlag: Int) = metadata and sensorFlag != 0

    fun getSensorCount(): Int {
        var count = 0
        for (i in SENSORS.indices) {
            if (hasSensor(SENSORS[i])) count += 1
        }
        return count
    }

    fun hasParticulateMatterData() = hasSensor(FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25)

    companion object {
        const val ERROR_CODE_SUCCESS = 0
        const val ERROR_CODE_LOCATION_MISSING = 1
        const val ERROR_CODE_NO_KNOWN_STATIONS = 2
        const val ERROR_CODE_STATIONS_TOO_FAR_AWAY = 3
        const val ERROR_CODE_AIR_QUALITY_MISSING = 4

        const val FLAG_SENSOR_PM10 = 1
        const val FLAG_SENSOR_PM25 = 2
        const val FLAG_SENSOR_O3 = 4
        const val FLAG_SENSOR_NO2 = 8
        const val FLAG_SENSOR_SO2 = 16
        const val FLAG_SENSOR_C6H6 = 32
        const val FLAG_SENSOR_CO = 64

        val SENSORS = listOf(
                FLAG_SENSOR_PM10,
                FLAG_SENSOR_PM25,
                FLAG_SENSOR_O3,
                FLAG_SENSOR_NO2,
                FLAG_SENSOR_SO2,
                FLAG_SENSOR_C6H6,
                FLAG_SENSOR_CO
        )
    }

}