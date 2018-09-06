package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface StationsDao {
    @Query("SELECT * FROM stations ORDER BY id ASC")
    fun getStations(): List<Station>

    @Insert
    fun insertStations(stations: List<Station>)
}