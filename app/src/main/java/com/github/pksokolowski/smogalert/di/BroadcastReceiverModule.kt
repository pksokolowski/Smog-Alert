package com.github.pksokolowski.smogalert.di

import com.github.pksokolowski.smogalert.alarms.AlarmReceiver
import com.github.pksokolowski.smogalert.alarms.BootFinishedReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class BroadcastReceiversModule {
    @ContributesAndroidInjector()
    abstract fun contributeAlarmReceiver(): AlarmReceiver

    @ContributesAndroidInjector()
    abstract fun contributeBootFinishedReceiver(): BootFinishedReceiver

//    @ContributesAndroidInjector()
//    abstract fun contributeNotificationsClickReceiver(): NotificationsClickReceiver
}