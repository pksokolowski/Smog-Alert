package com.github.pksokolowski.smogalert.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AirQualityLogsDao {
    @Query("SELECT * FROM air_quality_logs ORDER BY id DESC LIMIT 1")
    fun getLatestAirQualityLog(): AirQualityLog?

    @Query("SELECT * FROM air_quality_logs ORDER BY id DESC LIMIT 1")
    fun getLatestCachedAirQualityLog(): LiveData<AirQualityLog?>

    @Query("SELECT * FROM air_quality_logs ORDER BY id DESC LIMIT :n")
    fun getNLatestLogs(n: Int): List<AirQualityLog>

    @Insert
    fun insertAirQualityLog(log: AirQualityLog): Long
}