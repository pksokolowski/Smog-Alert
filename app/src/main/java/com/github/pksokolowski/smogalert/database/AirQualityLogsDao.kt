package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.*

@Dao
interface AirQualityLogsDao {
    @Query("SELECT * FROM air_quality_logs ORDER BY id DESC LIMIT 1")
    fun getLatestAirQualityLog(): AirQualityLog?

    @Insert
    fun insertAirQualityLog(log: AirQualityLog): Long
}