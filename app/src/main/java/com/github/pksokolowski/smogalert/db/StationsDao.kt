package com.github.pksokolowski.smogalert.db

import androidx.room.*

@Dao
interface StationsDao {
    @Query("SELECT * FROM stations ORDER BY id ASC")
    fun getStations(): List<Station>

    @Query("SELECT * FROM stations WHERE id = :id")
    fun getStationById(id: Long): Station?

    @Update
    fun updateStation(station: Station)

    @Insert
    fun insertStations(stations: List<Station>)

    @Update
    fun updateStations(stations: List<Station>)

    @Delete
    fun deleteStations(stations: List<Station>)
}