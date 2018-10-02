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
        val longitude: Double,

        @ColumnInfo(name = "absence_count")
        val absenceCount: Int = 0
) {
    fun assignSensors(sensorFlags: Int) =
            Station(id, sensorFlags, latitude, longitude, absenceCount)

    fun incrementAbsenceCount() =
            Station(id, sensorFlags, latitude, longitude, absenceCount + 1)

}