package com.github.pksokolowski.smogalert.di

import android.app.Application
import androidx.room.Room
import com.github.pksokolowski.smogalert.db.*
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
                .addCallback(CreateTriggersCallback)
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