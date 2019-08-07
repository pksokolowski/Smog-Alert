package com.github.pksokolowski.smogalert.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
        entities = [
            AirQualityLog::class,
            Station::class,
            StationsUpdateLog::class],
        version = 12,
        exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun airQualityLogsDao(): AirQualityLogsDao
    abstract fun stationsDao(): StationsDao
    abstract fun stationsUpdateLogsDao(): StationsUpdateLogsDao

}