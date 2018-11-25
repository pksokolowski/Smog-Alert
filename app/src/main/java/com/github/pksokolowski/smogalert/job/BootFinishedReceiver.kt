package com.github.pksokolowski.smogalert.job

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.android.AndroidInjection
import javax.inject.Inject

class BootFinishedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsValidator: SettingsValidator

    @Inject
    lateinit var settingsBackupHelper: SettingsBackupHelper

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        AndroidInjection.inject(this, context)

        if (action == ACTION_ANDROID_BOOT_COMPLETED) {
            settingsValidator.validate()

            // if sensitivity is set to 0, which means the app doesn't need to run in the background
            // this broadcastReceiver is disabled
            val sensitivity = settingsBackupHelper.getSensitivity()
            if (sensitivity == 0) {
                setBootFinishedReceiverEnabled(context, false)
            }
        }
    }

    companion object {
        const val ACTION_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"

        fun setBootFinishedReceiverEnabled(context: Context, enabled: Boolean) {
            val newState = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED

            val receiver = ComponentName(context, BootFinishedReceiver::class.java)
            val pm = context.packageManager

            pm.setComponentEnabledSetting(receiver,
                    newState,
                    PackageManager.DONT_KILL_APP)
        }
    }
}