package com.github.pksokolowski.smogalert.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component

@PerApp
@Component(modules = [AppModule::class, NetworkModule::class, ServiceModule::class, BroadcastReceiversModule::class, MainActivityModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: App)
}