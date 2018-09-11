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

        @ColumnInfo(name = "verdict")
        val verdict: Int = -1,

        @ColumnInfo(name = "raw_index")
        val indexRaw: Int = -1,

        @ColumnInfo(name = "station_id")
        val stationId: Long = -1,

        @ColumnInfo(name = "error_code")
        val errorCode: Int = 0,

        @ColumnInfo(name = "time_stamp")
        val timeStamp: Long

) {
    fun assignId(id: Long) =
            AirQualityLog(id, verdict, indexRaw, stationId, errorCode, timeStamp)

    companion object {
        const val ERROR_CODE_SUCCESS = 0
        const val ERROR_CODE_LOCATION_MISSING = 1
        const val ERROR_CODE_STATION_MISSING = 2
        const val ERROR_CODE_STATIONS_TOO_FAR_AWAY = 3
        const val ERROR_CODE_AIR_QUALITY_MISSING = 4

        const val VERDICT_ACCEPTABLE = 0
        const val VERDICT_ALARMING = 10
    }

}