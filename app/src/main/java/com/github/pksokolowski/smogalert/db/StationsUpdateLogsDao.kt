package com.github.pksokolowski.smogalert.db

import androidx.room.*

@Dao
interface StationsUpdateLogsDao {
    @Query("SELECT * FROM stations_update_logs ORDER BY id DESC LIMIT 1")
    fun getLastLog(): StationsUpdateLog?

    @Insert
    fun insertLog(log: StationsUpdateLog)
}