package com.github.pksokolowski.smogalert.di

import android.app.Application
import android.arch.persistence.room.Room
import com.github.pksokolowski.smogalert.database.AirQualityLogsDao
import com.github.pksokolowski.smogalert.database.AppDatabase
import com.github.pksokolowski.smogalert.database.StationsDao
import com.github.pksokolowski.smogalert.utils.DATABASE_NAME
import com.github.pksokolowski.smogalert.utils.ICacheMetadataHelper
import com.github.pksokolowski.smogalert.utils.STATIONS_CACHE_METADATA_FILE
import com.github.pksokolowski.smogalert.utils.SimpleMetadataHelper
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule

@Module(includes = [ViewModelModule::class, AndroidInjectionModule::class, AndroidSupportInjectionModule::class])
open class AppModule {

    @PerApp
    @Provides
    fun provideDb(app: Application): AppDatabase {
        return Room
                .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    @PerApp
    @Provides
    fun provideStationsDao(db: AppDatabase): StationsDao {
        return db.stationsDao()
    }

    @PerApp
    @Provides
    fun provideAirQualityLogsDao(db: AppDatabase): AirQualityLogsDao {
        return db.airQualityLogsDao()
    }

    @PerApp
    @Provides
    fun provideStationsRepoMetadataHelper(app: Application): ICacheMetadataHelper {
        return SimpleMetadataHelper(app, STATIONS_CACHE_METADATA_FILE)
    }

}