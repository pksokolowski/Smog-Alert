package com.github.pksokolowski.smogalert.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(
        entities = [
            AirQualityLog::class,
            Station::class,
            StationsUpdateLog::class],
        version = 11,
        exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun airQualityLogsDao(): AirQualityLogsDao
    abstract fun stationsDao(): StationsDao
    abstract fun stationsUpdateLogsDao(): StationsUpdateLogsDao

}