package com.github.pksokolowski.smogalert.alarms

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.android.AndroidInjection
import javax.inject.Inject

class BootFinishedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmHelper: AlarmHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action ?: return
        AndroidInjection.inject(this, context)

        if (action == ACTION_ANDROID_BOOT_COMPLETED) {
            alarmHelper.setNext()
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