package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(
        entities = [
            AirQualityLog::class,
            Station::class],
        version = 6,
        exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun airQualityLogsDao(): AirQualityLogsDao
    abstract fun stationsDao(): StationsDao

}