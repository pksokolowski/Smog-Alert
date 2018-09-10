package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.*

@Dao
interface StationsDao {
    @Query("SELECT * FROM stations ORDER BY id ASC")
    fun getStations(): List<Station>

    @Insert
    fun insertStations(stations: List<Station>)

    @Update
    fun updateStations(stations: List<Station>)

    @Delete
    fun deleteStations(stations: List<Station>)
}