package com.github.pksokolowski.smogalert.di

import com.github.pksokolowski.smogalert.job.BootFinishedReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class BroadcastReceiversModule {
    @ContributesAndroidInjector()
    abstract fun contributeBootFinishedReceiver(): BootFinishedReceiver
}