package com.github.pksokolowski.smogalert.di

import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule

@Module(includes = [ViewModelModule::class, BroadcastReceiversModule::class, AndroidInjectionModule::class, AndroidSupportInjectionModule::class])
open class AppModule {

//    @PerApp
//    @Provides
//    fun provideDb(app: Application): AppDatabase {
//        return Room
//                .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
//                .allowMainThreadQueries()
//                .addMigrations(AppDatabase.MIGRATION_1_2)
//                .build()
//    }
//
//    @PerApp
//    @Provides
//    fun provideReportsDao(db: AppDatabase): ReportsDao {
//        return db.daysDataDao()
//    }
//
//    @PerApp
//    @Provides
//    fun provideEditionsDao(db: AppDatabase): EditionsDao {
//        return db.editionsDao()
//    }
//
//    @PerApp
//    @Provides
//    fun provideGoalsDao(db: AppDatabase): GoalsDao {
//        return db.goalsDao()
//    }
}