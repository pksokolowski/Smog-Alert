package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.*

@Dao
interface StationsUpdateLogsDao {
    @Query("SELECT * FROM stations_update_log ORDER BY id DESC LIMIT 1")
    fun getLastLog(): StationsUpdateLog?

    @Insert
    fun insertLog(log: StationsUpdateLog)
}