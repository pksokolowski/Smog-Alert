package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(
        entities = [
            AirQualityLog::class,
            Station::class],
        version = 5,
        exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun airQualityLogsDao(): AirQualityLogsDao
    abstract fun stationsDao(): StationsDao

}