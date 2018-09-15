package com.github.pksokolowski.smogalert.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface AirQualityLogsDao {
    @Query("SELECT * FROM air_quality_logs ORDER BY id DESC LIMIT 1")
    fun getLatestAirQualityLog(): AirQualityLog?

    @Query("SELECT * FROM air_quality_logs ORDER BY id DESC LIMIT 1")
    fun getLatestCachedAirQualityLog(): LiveData<AirQualityLog?>

    @Insert
    fun insertAirQualityLog(log: AirQualityLog): Long
}