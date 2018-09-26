package com.github.pksokolowski.smogalert.di

import android.app.Application
import android.arch.persistence.room.Room
import com.github.pksokolowski.smogalert.database.AirQualityLogsDao
import com.github.pksokolowski.smogalert.database.AppDatabase
import com.github.pksokolowski.smogalert.database.StationsDao
import com.github.pksokolowski.smogalert.database.StationsUpdateLogsDao
import com.github.pksokolowski.smogalert.utils.DATABASE_NAME
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
    fun provideStationsUpdateLogsDao(db: AppDatabase): StationsUpdateLogsDao {
        return db.stationsUpdateLogsDao()
    }
}