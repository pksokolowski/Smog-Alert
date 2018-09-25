package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "stations")
data class Station(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "sensor_flags")
        val sensorFlags: Int,

        @ColumnInfo(name = "latitude")
        val latitude: Double,

        @ColumnInfo(name = "longitude")
        val longitude: Double
) {
    fun assignSensors(sensorFlags: Int) =
            Station(id, sensorFlags, latitude, longitude)
}